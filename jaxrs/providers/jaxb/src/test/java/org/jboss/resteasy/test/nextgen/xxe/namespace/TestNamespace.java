package org.jboss.resteasy.test.nextgen.xxe.namespace;

import static org.jboss.resteasy.test.TestPortProvider.generateURL;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlRootElement;

import junit.framework.Assert;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.spi.ResteasyDeployment;
import org.jboss.resteasy.test.EmbeddedContainer;
import org.junit.After;
import org.junit.Test;

/**
 * Unit tests for RESTEASY-996.
 * 
 *
 * @author <a href="mailto:ron.sigal@jboss.com">Ron Sigal</a>
 * @date Dec 25, 2013
 */
public class TestNamespace
{
   protected static ResteasyDeployment deployment;
   protected static Dispatcher dispatcher;
   protected static ResteasyClient client;

   @Path("/")
   public static class MovieResource
   {
     @POST
     @Path("xmlRootElement")
     @Consumes({"application/xml"})
     public String addFavoriteMovie(FavoriteMovieXmlRootElement movie)
     {
        System.out.println("MovieResource(xmlRootElment): title = " + movie.getTitle());
        return movie.getTitle();
     }
     
     @POST
     @Path("xmlType")
     @Consumes({"application/xml"})
     public String addFavoriteMovie(FavoriteMovieXmlType movie)
     {
        System.out.println("MovieResource(xmlType): title = " + movie.getTitle());
        return movie.getTitle();
     }
     
     @POST
     @Path("JAXBElement")
     @Consumes("application/xml")
     public String addFavoriteMovie(JAXBElement<FavoriteMovie> value)
     {
        System.out.println("MovieResource(JAXBElement): title = " + value.getValue().getTitle());
        return value.getValue().getTitle();
     }
     
     @POST
     @Path("list")
     @Consumes("application/xml")
     public String addFavoriteMovie(List<FavoriteMovieXmlRootElement> list)
     {
        String titles = "";
        Iterator<FavoriteMovieXmlRootElement> it = list.iterator();
        while (it.hasNext())
        {
           String title = it.next().getTitle();
           System.out.println("MovieResource(list): title = " + title);
           titles += "/" + title;
        }
        return titles;
     }
     
     @POST
     @Path("set")
     @Consumes("application/xml")
     public String addFavoriteMovie(Set<FavoriteMovieXmlRootElement> set)
     {
        String titles = "";
        Iterator<FavoriteMovieXmlRootElement> it = set.iterator();
        while (it.hasNext())
        {
           String title = it.next().getTitle();
           System.out.println("MovieResource(list): title = " + title);
           titles += "/" + title;
        }
        return titles;
     }
     
     @POST
     @Path("array")
     @Consumes("application/xml")
     public String addFavoriteMovie(FavoriteMovieXmlRootElement[] array)
     {
        String titles = "";
        for (int i = 0; i < array.length; i++)
        {
           String title = array[i].getTitle();
           System.out.println("MovieResource(list): title = " + title);
           titles += "/" + title;
        }
        return titles;
     }
     
     @POST
     @Path("map")
     @Consumes("application/xml")
     public String addFavoriteMovie(Map<String,FavoriteMovieXmlRootElement> map)
     {
        String titles = "";
        Iterator<String> it = map.keySet().iterator();
        while (it.hasNext())
        {
           String title = map.get(it.next()).getTitle();
           System.out.println("MovieResource(map): title = " + title);
           titles += "/" + title;
        }
        return titles;
     }
   }
   
   @XmlRootElement
   public static class FavoriteMovieXmlRootElement {
     private String _title;
     public String getTitle() {
       return _title;
     }
     public void setTitle(String title) {
       _title = title;
     }
   }

   public static void before() throws Exception
   {
      Hashtable<String,String> initParams = new Hashtable<String,String>();
      Hashtable<String,String> contextParams = new Hashtable<String,String>();
      contextParams.put("resteasy.document.expand.entity.references", "false");
      deployment = EmbeddedContainer.start(initParams, contextParams);
      dispatcher = deployment.getDispatcher();
      deployment.getRegistry().addPerRequestResource(MovieResource.class);
      client = new ResteasyClientBuilder().build();
   }
   
   @After
   public void after() throws Exception
   {
      EmbeddedContainer.stop();
      dispatcher = null;
      deployment = null;
      client.close();
   }

   @Test
   public void testXmlRootElement() throws Exception
   {
      before();
      ResteasyWebTarget target = client.target(generateURL("/xmlRootElement"));
      FavoriteMovieXmlRootElement movie = new FavoriteMovieXmlRootElement();
      movie.setTitle("La Regle du Jeu");
      Response response = target.request().post(Entity.entity(movie,"application/xml"));
      Assert.assertEquals(200, response.getStatus());
      String entity = response.readEntity(String.class);
      System.out.println("Result: " + entity);
      Assert.assertEquals("La Regle du Jeu", entity);
   }
   
   @Test
   public void testXmlType() throws Exception
   {
      before();
      
      ResteasyWebTarget target = client.target(generateURL("/xmlType"));
      FavoriteMovieXmlType movie = new FavoriteMovieXmlType();
      movie.setTitle("La Cage Aux Folles");
      Response response = target.request().post(Entity.entity(movie,"application/xml"));
      Assert.assertEquals(200, response.getStatus());
      String entity = response.readEntity(String.class);
      System.out.println("Result: " + entity);
      Assert.assertEquals("La Cage Aux Folles", entity);
   }
   
   @Test
   public void testJAXBElement() throws Exception
   {
      before(); 
      ResteasyWebTarget target = client.target(generateURL("/JAXBElement"));
      String str = "<?xml version=\"1.0\"?>\r" +
                   "<favoriteMovieXmlType xmlns=\"http://abc.com\"><title>La Cage Aux Folles</title></favoriteMovieXmlType>";
      System.out.println(str);
      Response response = target.request().post(Entity.entity(str,"application/xml"));
      Assert.assertEquals(200, response.getStatus());
      String entity = response.readEntity(String.class);
      System.out.println("Result: " + entity);
      Assert.assertEquals("La Cage Aux Folles", entity);
   }
   
   @Test
   public void testList() throws Exception
   {
      doCollectionTest("list");
   }
   
   @Test
   public void testSet() throws Exception
   {
      doCollectionTest("set");
   }
   
   @Test
   public void testArray() throws Exception
   {
      doCollectionTest("array");
   }

   @Test
   public void testMap() throws Exception
   {
      doMapTest();
   }
   
   void doCollectionTest(String path) throws Exception
   {
      before();
      
      ResteasyWebTarget target = client.target(generateURL("/" + path));
      String str = "<?xml version=\"1.0\"?>\r" +
                   "<collection xmlns=\"http://abc.com\">" +
                   "<favoriteMovieXmlRootElement><title>La Cage Aux Folles</title></favoriteMovieXmlRootElement>" +
                   "<favoriteMovieXmlRootElement><title>La Regle du Jeu</title></favoriteMovieXmlRootElement>" +
                   "</collection>";
      System.out.println(str);
      Response response = target.request().post(Entity.entity(str,"application/xml"));
      Assert.assertEquals(200, response.getStatus());
      String entity = response.readEntity(String.class);
      System.out.println("Result: " + entity);
      if (entity.indexOf("Cage") < entity.indexOf("Regle"))
      {
         Assert.assertEquals("/La Cage Aux Folles/La Regle du Jeu", entity);
      }
      else
      {
         Assert.assertEquals("/La Regle du Jeu/La Cage Aux Folles", entity);
      }
   }
   
   void doMapTest() throws Exception
   {
      before();
      
      ResteasyWebTarget target = client.target(generateURL("/map"));
      String str = "<?xml version=\"1.0\"?>\r" +
                   "<map xmlns=\"http://abc.com\">" +
                     "<entry key=\"new\">" +
                       "<favoriteMovieXmlRootElement><title>La Cage Aux Folles</title></favoriteMovieXmlRootElement>" +
                     "</entry>" +
                     "<entry key=\"old\">" +
                       "<favoriteMovieXmlRootElement><title>La Regle du Jeu</title></favoriteMovieXmlRootElement>" +
                     "</entry>" +
                   "</map>";
      System.out.println(str);
      Response response = target.request().post(Entity.entity(str,"application/xml"));
      Assert.assertEquals(200, response.getStatus());
      String entity = response.readEntity(String.class);
      System.out.println("Result: " + entity);
      if (entity.indexOf("Cage") < entity.indexOf("Règle"))
      {
         Assert.assertEquals("/La Cage Aux Folles/La Regle du Jeu", entity);
      }
      else
      {
         Assert.assertEquals("/La Regle du Jeu/La Cage Aux Folles", entity);
      }
   }
}
