const WebSocket = require('ws');
const schema = require('enigma.js/schemas/12.20.0.json');
const enigma = require('enigma.js');

const host = process.argv.slice(2)[0] || 'localhost';
const port = host === 'localhost' ? 19076 : 9076;

// create a new session:
const session = enigma.create({
  schema,
  url: `ws://${host}:${port}/app/engineData`,
  createSocket(url) {
    return new WebSocket(url);
  },
});

let global;
let app;
let reloadRequestId;
const appId = 'reloadapp.qvf';
let connectionId;
const trafficLog = false;

if (trafficLog) {
  // bind traffic events to log what is sent and received on the socket:
  session.on('traffic:sent', data => console.log('sent:', data));
  session.on('traffic:received', data => console.log('received:', data));
}

session
  .open()
  .then((_global) => {
    global = _global;
    console.log('Creating/opening app');
    return global
      .createApp(appId)
      .then(appInfo => global.openDoc(appInfo.qAppId))
      .catch(() => global.openDoc(appId));
  })
  .then((_app) => {
    console.log('Creating connection');
    app = _app;
    return app.createConnection({
      qType: 'jdbc', // the name we defined as a parameter to engine in our docker-compose.yml
      qName: 'jdbc',
      qConnectionString:
        'CUSTOM CONNECT TO "provider=jdbc;driver=postgresql;host=192.168.0.77;port=54321;database=postgres"', // the connection string inclues both the provide to use and parameters to it.
      qUserName: 'postgres', // username and password for the postgres database, provided to the GRPC-Connector
      qPassword: 'postgres',
    });
  })
  .then((_connectionId) => {
      connectionId = _connectionId;
      console.log('Setting script');
      const script = `
      lib connect to 'jdbc';
      AllTypes:
      sql SELECT * FROM all_types;
      `; // add script to use the grpc-connector and load a table
      return app.setScript(script);
    })
  .then(() => {
    console.log('Reloading');
    const reloadPromise = app.doReload();
    reloadRequestId = reloadPromise.requestId;
    return reloadPromise;
  })
  .then(() => global.getProgress(reloadRequestId))
  .then(() => {})
  .then(() => {
    console.log('Removing connection before saving');
    return app.deleteConnection(connectionId);
  })
  .then(() => {
    console.log('Removing script before saving');
    return app.setScript('');
  })
  .then(() => {
    console.log('Saving');
    return app.doSave();
  })
  .then(() => {
    //  type_small_int,type_integer,type_bigint,type_bit,type_numeric,type_double,type_real,type_decimal,type_boolean,type_date,type_time,type_timestamp,type_text,type_varchar,type_char
    return app.getFieldDescription("type_date");
//    console.log('Fetching Table sample');
//    return app.getTableData(-1, 10000, true, 'AllTypes');
//  })
//  .then((tableData) => {
//    if (tableData.length === 0) {
//      return Promise.reject('Empty table response');
//    }
////
////    // Check if the first row contains what is expected. Exclude the last date column since it varies.
////    const firstDataRow = tableData[1].qValue
////      .map(obj => obj.qText)
////      .reduce((a, b) => `${a}:${b}`);
////    const expectedFirstDataRowExcludingDate =
////      '4316:7 Novembre:Tabarka:Tunisia:TBJ:DTKA:36.978333:8.876389:0:1:E:Africa/Tunis';
////    if (firstDataRow.lastIndexOf(expectedFirstDataRowExcludingDate) !== 0) {
////      return Promise.reject(
////        'The check on the first row content was unsuccessful got: ' + firstDataRow,
////      );
////    }
//
//    // Convert table grid into a string using some functional magic
//    const tableDataAsString = tableData
//      .map(row =>
//        row.qValue
//          .map(value => value.qText)
//          .reduce((left, right) => `${left}\t${right}`),
//      )
//      .reduce((row1, row2) => `${row1}\n${row2}`);
//    console.log(tableDataAsString);
//    return Promise.resolve();
  })
  .then((fieldData) => {
    console.log(fieldData);
    return Promise.resolve();
  })
  .then(() => session.close())
  .then(() => console.log('Session closed'))
  .catch((err) => {
    console.log('Something went wrong :(', err);
    process.exit(1);
  });
