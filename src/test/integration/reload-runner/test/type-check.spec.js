const expect = require('chai').expect;

const WebSocket = require('ws');
const schema = require('enigma.js/schemas/12.20.0.json');
const enigma = require('enigma.js');

const host = process.env.ENGINE_HOST || 'localhost';
const port = 19076;

// create a new session:
const session = enigma.create({
  schema,
  url: `ws://${host}:${port}/app/engineData`,
  createSocket(url) {
    return new WebSocket(url);
  },
});

const appId = 'reloadapp.qvf';

describe('Type check', async () => {
  it('should have correct type in QIX', async () => {
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
            `CUSTOM CONNECT TO "provider=jdbc;driver=postgresql;host=postgres-database;port=5432;database=postgres"`, // the connection string inclues both the provide to use and parameters to it.
      qUserName: 'postgres', // username and password for the postgres database, provided to the GRPC-Connector
      qPassword: 'postgres',
    });

    const script = `
        lib connect to 'jdbc';
        AllTypes:
        sql SELECT * FROM all_types;
        `;
    await app.setScript(script);

    const reloadRequestId = await app.doReload().requestId;
    await global.getProgress(reloadRequestId);
    await app.deleteConnection(connectionId);
    await app.setScript('');
    await app.doSave();

    let fieldInfo = await app.getFieldDescription('type_small_int');
    expect(fieldInfo.qTags).to.include('$integer');

    fieldInfo = await app.getFieldDescription('type_integer');
    expect(fieldInfo.qTags).to.include('$integer');

    fieldInfo = await app.getFieldDescription('type_bigint');
    expect(fieldInfo.qTags).to.include('$integer');

    fieldInfo = await app.getFieldDescription('type_bit');
    expect(fieldInfo.qTags).to.include('$integer');

    fieldInfo = await app.getFieldDescription('type_numeric');
    expect(fieldInfo.qTags).to.include('$numeric');

    fieldInfo = await app.getFieldDescription('type_double');
    expect(fieldInfo.qTags).to.include('$numeric');

    fieldInfo = await app.getFieldDescription('type_real');
    expect(fieldInfo.qTags).to.include('$numeric');

    fieldInfo = await app.getFieldDescription('type_decimal');
    expect(fieldInfo.qTags).to.include('$numeric');

    fieldInfo = await app.getFieldDescription('type_boolean');
    expect(fieldInfo.qTags).to.include('$integer');

    fieldInfo = await app.getFieldDescription('type_date');
    expect(fieldInfo.qTags).to.include('$timestamp');

//        // Not getting the right type from QIX
//        fieldInfo = await app.getFieldDescription("type_time");
//        expect(fieldInfo.qTags).to.include('$timestamp');

    fieldInfo = await app.getFieldDescription('type_timestamp');
    expect(fieldInfo.qTags).to.include('$timestamp');

    fieldInfo = await app.getFieldDescription('type_text');
    expect(fieldInfo.qTags).to.include('$text');

    fieldInfo = await app.getFieldDescription('type_varchar');
    expect(fieldInfo.qTags).to.include('$text');

    fieldInfo = await app.getFieldDescription('type_char');
    expect(fieldInfo.qTags).to.include('$text');

    session.close();
  });
});
