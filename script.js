const darkModeToggle = document.getElementById('darkModeToggle');
const body = document.body;

darkModeToggle.addEventListener('click', () => {
    body.classList.toggle('dark-mode');
});

// Fetch recommendations when the page loads
window.addEventListener('load', async () => {
    const recommendations = await fetchRecommendations();
    displayVideos(recommendations);
});

document.getElementById('searchForm').addEventListener('submit', async (event) => {
    event.preventDefault(); // Prevent form submission

    const searchQuery = document.getElementById('searchInput').value;
    const videos = await fetchVideos(searchQuery);
    displayVideos(videos);
});

async function fetchRecommendations() {
    // Fetch recommendations from YouTube
    const response = await fetch('https://www.googleapis.com/youtube/v3/videos?part=snippet&chart=mostPopular&maxResults=5&key=AIzaSyCeS9K8VgeanxqPAks2pdWI8XANAG9L7TA&part=snippet&type=video&maxResults=4');
    const data = await response.json();
    return data.items; // Array of recommended videos
}

async function fetchVideos(searchQuery) {
    // Fetch videos using the YouTube Data API
    const response = await fetch(`https://www.googleapis.com/youtube/v3/search?q=${searchQuery}&key=AIzaSyCeS9K8VgeanxqPAks2pdWI8XANAG9L7TA&part=snippet&type=video&maxResults=4`);
    const data = await response.json();
    return data.items; // Array of video items
}

function displayVideos(videos) {
    const videosContainer = document.getElementById('videosContainer');
    videosContainer.innerHTML = ''; // Clear previous videos

    videos.forEach(video => {
        const videoElement = document.createElement('iframe');
        videoElement.src = `https://www.youtube.com/embed/${video.id.videoId}`;
        videoElement.width = 560;
        videoElement.height = 315;
        videoElement.title = video.snippet.title;
        videoElement.allowFullscreen = true;
        videosContainer.appendChild(videoElement);
    });
}
