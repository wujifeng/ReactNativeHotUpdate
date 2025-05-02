const http = require('http');
const fs = require('fs');

const server = http.createServer((req, res) => {
  if (req.url === '/index.android.bundle') {
    fs.readFile('./index.android.bundle', (err, data) => {
      if (err) {
        console.error('Failed to read index.android.bundle:', err);
        res.writeHead(500);
        res.end('Internal Server Error');
        return;
      }

      res.writeHead(200, {
        'Content-Type': 'application/javascript', // Adjust according to the actual MIME type of the bundle
        'Content-Length': data.length,
      });
      res.end(data);
    });
  } else {
    res.writeHead(404);
    res.end('Not Found');
  }
});

server.listen(80, () => {
  console.log('HTTP server running on port 8080， http://10.56.238.168:80/index.android.bundle');
});