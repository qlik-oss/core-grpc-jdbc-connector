const WebSocket = require('ws');
const schema = require('enigma.js/schemas/12.20.0.json');
const enigma = require('enigma.js');

const host = process.env.ENGINE_HOST|| 'localhost';
const port = 19076;

// create a new session:
const session = enigma.create({
  schema,
  url: `ws://${host}:${port}/app/engineData`,
  createSocket(url) {
    return new WebSocket(url);
  },
});

async function loadData() {
  const appId = 'reloadapp.qvf';
  const startTime = Date.now();
  const global = await session.open();
  let app;

  try {
    const appInfo = await global.createApp(appId);
    app = await global.openDoc(appInfo.qAppId);
  } catch (e) {
    app = await global.openDoc(appId);
  }

  const connectionId = await app.createConnection({
    qType: 'jdbc', // the name we defined as a parameter to engine in our docker-compose.yml
    qName: 'jdbc',
    qConnectionString:
        'CUSTOM CONNECT TO "provider=jdbc;driver=postgresql;host=postgres-database;port=5432;database=postgres"', // the connection string inclues both the provide to use and parameters to it.
    qUserName: 'postgres', // username and password for the postgres database, provided to the GRPC-Connector
    qPassword: 'postgres',
  });

  const script = `
    lib connect to 'jdbc';
    airports:
    sql SELECT * FROM airports;
    `;
  await app.setScript(script);

  const reloadRequestId = await app.doReload().requestId;
  await global.getProgress(reloadRequestId);

  console.log(`Reload took: ${Date.now() - startTime} ms`);

  await app.deleteConnection(connectionId);
  await app.setScript('');
  await app.doSave();


  const tableData = await app.getTableData(-1, 10000, true, 'airports');

  const tableDataAsString = tableData
      .map(row =>
        row.qValue
          .map(value => value.qText)
          .reduce((left, right) => `${left}\t${right}`),
      )
      .reduce((row1, row2) => `${row1}\n${row2}`);

  console.log(tableDataAsString);

  session.close();
}

loadData();
