import React, { useState, useRef, useEffect  } from 'react';
import axios from 'axios';

function App() {
  const [MBID, setMBID] = useState('');
  const [responseData, setResponseData] = useState(null);
  const responseRef = useRef(null);

  const handleSubmit = async (event) => {
    event.preventDefault();

    try {
      const response = await axios.get(`http://localhost:8080/api/${MBID}`);
      setResponseData(response.data);
    } catch (error) {
      console.error(error);
    }
  };

  const handleInputChange = (event) => {
    setMBID(event.target.value);
  };

  useEffect(() => {
    if (responseData) {
      responseRef.current.scrollIntoView({ behavior: "smooth" });
    }
  }, [responseData]);

  return (
    <div>
      <div id="search-mbid">
        <h1>Mashup API</h1>
        <form onSubmit={handleSubmit}>
          <label>
            <input type="text" placeholder="MBID" value={MBID} onChange={handleInputChange} />
          </label>
          <button type="submit">Submit</button>
        </form>
      </div>
      {responseData && (
        <div ref={responseRef}>
          <h2 id="album_header">API Response:</h2>
          <h3 id="album_header">MBID: {responseData.mbid}</h3>
          <div id="description">
            <p dangerouslySetInnerHTML={{__html: responseData.description}}></p>
          </div>
          <div id="albumContainer">
            <h3 id="album_header">Albums:</h3>
            <ul class="image-gallery">
              {responseData.albums.map(album => (
                <li key={album.id}>
                  <h4>{album.title}</h4>
                  {album.coverArt !== "No cover art found" ? (
                    <img src={album.coverArt} alt={album.title} />
                  ) : (
                    <p>{album.coverArt}</p>
                  )}
                </li>
              ))}
            </ul>
          </div>
        </div>
      )}

    </div>
  );
}

export default App;
