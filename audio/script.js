const darkModeToggle = document.getElementById('darkModeToggle');
const body = document.body;

darkModeToggle.addEventListener('change', () => {
    if (darkModeToggle.checked) {
        body.classList.add('dark-mode'); // Add dark mode class to body
    } else {
        body.classList.remove('dark-mode'); // Remove dark mode class from body
    }
});

// Function to fetch suggested songs from Spotify API
async function fetchSuggestedSongs() {
    const clientId = 'c8f496e4395645b7aa27352c45a2d624'; // Replace with your Spotify API client ID
    const clientSecret = 'bf1be09231204fe59f9282c344071fb5'; // Replace with your Spotify API client secret

    // Step 1: Get access token
    const tokenResponse = await fetch('https://accounts.spotify.com/api/token', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
            'Authorization': 'Basic ' + btoa(clientId + ':' + clientSecret)
        },
        body: 'grant_type=client_credentials'
    });

    const tokenData = await tokenResponse.json();
    const accessToken = tokenData.access_token;

    // Step 2: Use access token to fetch suggested songs
    const response = await fetch('https://api.spotify.com/v1/browse/new-releases', {
        headers: {
            'Authorization': 'Bearer ' + accessToken
        }
    });

    const responseData = await response.json();
    const songs = responseData.albums.items; // Assuming you want to fetch new releases

    return songs;
}

// Function to initialize the page with suggested songs
async function initializePageWithSuggestedSongs() {
    try {
        // Fetch suggested songs from Spotify API
        const songs = await fetchSuggestedSongs();
        
        // Display the fetched songs
        displaySongs(songs);
    } catch (error) {
        console.error('Error fetching suggested songs:', error);
    }
}

// Call the initializePageWithSuggestedSongs function when the page loads
window.addEventListener('load', initializePageWithSuggestedSongs);

// Function to play a song
function playSong(previewUrl) {
    const audioPlayer = document.getElementById('audioPlayer');
    if (previewUrl) {
        audioPlayer.src = previewUrl;
        audioPlayer.play();
    } else {
        console.error('No preview available for this song.');
    }
}

// Function to display fetched songs
function displaySongs(songs) {
    const songsList = document.getElementById('songsList');

    // Clear previous results
    songsList.innerHTML = '';

    // Display each song
    songs.forEach(song => {
        const songItem = document.createElement('div');
        songItem.classList.add('song');

        const coverImg = document.createElement('img');
        coverImg.src = song.images[0].url; // Use the first image as cover
        coverImg.alt = song.name;
        songItem.appendChild(coverImg);

        const songInfo = document.createElement('div');
        songInfo.classList.add('song-info');

        const songName = document.createElement('h3');
        songName.textContent = song.name;
        songInfo.appendChild(songName);

        // For demonstration purposes, let's assume each song has only one artist
        const artistName = document.createElement('p');
        artistName.textContent = song.artists[0].name;
        songInfo.appendChild(artistName);

        // Play button
        const playButton = document.createElement('button');
        playButton.textContent = 'Play';
        playButton.addEventListener('click', () => {
            playSong(song.preview_url);
        });
        songInfo.appendChild(playButton);

        songItem.appendChild(songInfo);

        songsList.appendChild(songItem);
    });
}
