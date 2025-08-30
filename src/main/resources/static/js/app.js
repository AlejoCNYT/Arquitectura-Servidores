function loadGetMsg() {
    const name = document.getElementById('name').value;
    fetch('/app/hello?name=' + name)
        .then(response => response.json())
        .then(data => {
            document.getElementById('getrespmsg').innerHTML = data.message;
        })
        .catch(err => {
            document.getElementById('getrespmsg').innerHTML = 'Error: ' + err;
        });
}

function fetchStockData() {
    const symbol = document.getElementById('stockSymbol').value;
    document.getElementById('stockData').innerHTML = "Loading...";
    
    fetch('/stocks?symbol=' + symbol)
        .then(response => response.json())
        .then(data => {
            document.getElementById('stockData').innerHTML = 
                JSON.stringify(data, null, 2);
        })
        .catch(err => {
            document.getElementById('stockData').innerHTML = 'Error: ' + err;
        });
}
