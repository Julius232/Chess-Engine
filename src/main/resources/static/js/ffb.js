const makeRequest = async (method, url, callback) => {
  try {
    const response = await fetch(url, { method });
    if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
    const data = await response.json();
    if (callback) callback(data);
  } catch (error) {
    console.error('Request failed:', error);
  }
};

const checkState = (state) => {
  if (state !== "PLAY") {
    document.getElementById("header").textContent = state;
  }
};

const updateScore = () => {
  makeRequest('GET', 'http://localhost:8080/chess/score', (data) => {
    document.getElementById("scoreWhite").textContent = `WHITE: ${data.scoreWhite}`;
    document.getElementById("scoreBlack").textContent = `BLACK: ${data.scoreBlack}`;
  });
};

const makeMove = (type, color) => {
  makeRequest('PATCH', `http://localhost:8080/chess/figure/move/${type}/${color}`, (data) => {
    checkState(data.state);
    reloadBoard();
  });
};

const onDrop = (source, target) => {
  makeRequest('PATCH', `http://localhost:8080/chess/figure/move/${source}/${target}`, reloadBoard);
};

const reloadBoard = () => {
  makeRequest('GET', 'http://localhost:8080/chess/figure/frontend', (data) => {
    board.position(data.renderBoard);
    updateScore();
  });
};

const highlightSquare = (square, highlight) => {
  const $square = $(`#board .square-${square}`);
  const background = $square.hasClass('black-3c85d') ? 'lightskyblue' : 'blue';
  $square.css('background', highlight ? background : '');
};

const onMouseoverSquare = (square) => {
  makeRequest('GET', `http://localhost:8080/chess/figure/move/possible/${square}`, (moves) => {
    if (moves.length === 0) return;
    highlightSquare(square, true);
    moves.forEach(move => highlightSquare(move.x + move.y, true));
  });
};

const onMouseoutSquare = () => {
  $('#board .square-55d63').css('background', '');
};

const initEventListeners = () => {
  $('#computerMove').on('click', () => makeMove('intelligent', 'black'));
  $('#resetBoard').on('click', () => makeRequest('PUT', 'http://localhost:8080/chess/reset', reloadBoard));
};

const boardConfig = {
  draggable: true,
  onDrop: (source, target) => onDrop(source, target),
  onMouseoverSquare: (square, piece) => onMouseoverSquare(square, piece),
  onMouseoutSquare: (square, piece) => onMouseoutSquare(square, piece)
};

// Function to start the auto-refresh interval
const startAutoRefresh = (intervalMs) => {
  setInterval(() => {
    reloadBoard();
  }, intervalMs);
};

const board = Chessboard('board', boardConfig);

initEventListeners();
reloadBoard();
startAutoRefresh(300);