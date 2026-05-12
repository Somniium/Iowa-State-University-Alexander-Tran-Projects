## Albums
### Get
### All Get Requests return ResponseEntity<>
#### 1. /albums
Retrieves all albums that are currently in the database.
#### 2. /search-albums-and-save
Uses the Spotify API to search for an album and save it to the database.
Returns the Album Java object
(Use this request to add an album to the DB when user searches for it and
it doesn't exist. USE NEXT GET REQUESTS FOR AN ALBUM THAT IS ALREADY IN THE DB)
#### 3. /album/{albumId}
Checks the database for an album with a given ID.
#### 4. /album/name(?name={albumName})
Checks the database for an album with given name.
### Post
#### 1. /add-album(?album_name={albumName})
Searches for an album by name and adds it to the DB.
### Put
#### 1. /album/{albumId}
Updates an album using the DB ID.
### Delete
#### 1. /album/{albumId}
Deletes an album using the DB ID.