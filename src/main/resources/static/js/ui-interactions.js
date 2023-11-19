$(document).ready(function () {
    let computerColor = 'black'; // Default computer color

    // Event listeners for UI interactions
    const initEventListeners = () => {
        $('#computerMove').on('click', () => {
            makeMove('intelligent', computerColor); // Assuming makeMove is defined in chess-data-fetching.js
        });
        $('#resetBoard').on('click', () => {
            makeRequest('PUT', 'http://localhost:8080/chess/reset', reloadBoard); // Assuming makeRequest and reloadBoard are defined in chess-data-fetching.js
        });
        $('#undoMove').on('click', () => {
            makeRequest('GET', 'http://localhost:8080/chess/undo', reloadBoard); // Assuming makeRequest and reloadBoard are defined in chess-data-fetching.js
        });
        $('#autoPlay').on('click', () => {
            makeRequest('GET', 'http://localhost:8080/chess/autoplay', reloadBoard); // Assuming makeRequest and reloadBoard are defined in chess-data-fetching.js
        });
        $('#importFEN').on('click', function () {
            var fenString = prompt("Please enter FEN:");
            if (fenString) {
                importFEN(fenString);
            }
        });
    };

    // Function to set board orientation based on color choice
    const setBoardOrientation = (color) => {
        board.orientation(color); // Set board orientation to chosen color
    };

    const chooseColor = (color) => {
        setBoardOrientation(color); // Set orientation based on user choice
        computerColor = (color === 'white') ? 'black' : 'white'; // Computer plays opposite color
    };

    const initColorChoiceEventListeners = () => {
        $('#playWhite').on('click', () => chooseColor('white'));
        $('#playBlack').on('click', () => chooseColor('black'));
    };
    
    // Initialize event listeners
    initEventListeners();
    initColorChoiceEventListeners();
    setupModal();
});