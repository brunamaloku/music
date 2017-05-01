package com.sismics.music.rest;

import com.google.common.collect.ImmutableMap;
import org.junit.Assert;
import org.junit.Test;

import javax.json.JsonArray;
import javax.json.JsonObject;

/**
 * Exhaustive test of the directory resource.
 * 
 * @author jtremeaux
 */
public class TestDirectoryResource extends BaseMusicTest {
    /**
     * Test the directory resource.
     */
    @Test
    public void testDirectoryResource() throws Exception {
        // Create alice user
        createUser("alice");

        // Alice lists the directories: access to this resource is forbidden
        login("alice");
        GET("/directory");
        assertIsForbidden();

        // Admin creates a directory : bad request (location required)
        loginAdmin();
        PUT("/directory");
        assertIsBadRequest();
        JsonObject json = response.readEntity(JsonObject.class);
        Assert.assertEquals("ValidationError", json.getString("type"));
        Assert.assertTrue(json.getString("message"), json.getString("message").contains("location must be set"));

        // Admin creates a directory : OK
        PUT("/directory", ImmutableMap.of("location", "/vartest/music/main"));
        assertIsOk();

        // Admin creates a directory : OK
        PUT("/directory", ImmutableMap.of("location", "/vartest/music/mix"));
        assertIsOk();

        // Admin lists all directories
        GET("/directory");
        assertIsOk();
        json = getJsonResult();
        JsonArray directories = json.getJsonArray("directories");
        Assert.assertNotNull(directories);
        Assert.assertEquals(2, directories.size());
        JsonObject directory0 = directories.getJsonObject(0);
        String directory0Id = directory0.getString("id");
        Assert.assertNotNull(directory0Id);
        Assert.assertNotNull("/var/music/main", directory0.getString("location"));
        Assert.assertTrue(directory0.getBoolean("active"));
        Assert.assertFalse(directory0.getBoolean("valid"));
        String directory1Id = directories.getJsonObject(1).getString("id");

        // Admin updates the directory info
        POST("/directory/" + directory0Id, ImmutableMap.of(
                "location", "/vartest/music/mainstream",
                "active", "true"));
        assertIsOk();

        // Check the update
        GET("/directory");
        assertIsOk();
        json = getJsonResult();
        directories = json.getJsonArray("directories");
        Assert.assertNotNull(directories);
        Assert.assertEquals(2, directories.size());
        directory0 = directories.getJsonObject(0);
        Assert.assertNotNull(directory0.getString("id"));
        Assert.assertNotNull("/var/music/mainstream", directory0.getString("location"));
        Assert.assertTrue(directory0.getBoolean("active"));

        // Admin deletes the directories
        DELETE("/directory/" + directory0Id);
        assertIsOk();

        DELETE("/directory/" + directory1Id);
        assertIsOk();

        // Check the deletion
        GET("/directory");
        assertIsOk();
        json = getJsonResult();
        directories = json.getJsonArray("directories");
        Assert.assertNotNull(directories);
        Assert.assertEquals(0, directories.size());
    }

    /**
     * Test the collection indexing service.
     *
     */
    @Test
    public void testCollectionIndexing() throws Exception {
        // Login users
        loginAdmin();

        // Admin adds a directory to the collection
        addDirectory("/music/");

        // Admin lists all directories
        GET("/directory");
        assertIsOk();
        JsonObject json = getJsonResult();
        JsonArray directories = json.getJsonArray("directories");
        Assert.assertNotNull(directories);
        Assert.assertEquals(1, directories.size());
        JsonObject directory0 = directories.getJsonObject(0);
        String directory0Id = directory0.getString("id");

        // Check that the albums are correctly added
        GET("/album");
        assertIsOk();
        json = getJsonResult();
        JsonArray albums = json.getJsonArray("albums");
        Assert.assertNotNull(albums);
        Assert.assertEquals(2, albums.size());

        // Admin deletes the directory
        DELETE("/directory/" + directory0Id);
        assertIsOk();

        // Check that the albums are correctly removed
        GET("/album");
        assertIsOk();
        json = getJsonResult();
        albums = json.getJsonArray("albums");
        Assert.assertNotNull(albums);
        Assert.assertEquals(0, albums.size());
    }
}
