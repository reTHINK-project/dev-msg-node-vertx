import expect from 'expect.js';
import activate from '../src/js/client/VertxProtoStub';

describe('Cluster', function() {
  it('cluster connectivity', function(done) {
    let protoAliceURL = 'hyperty-runtime://sp1/protostub/alice';
    let protoBobURL = 'hyperty-runtime://sp1/protostub/bob';

    //TODO: requirement -> vertx MN must be online on (ws://localhost:9090/ws, ws://localhost:9091/ws)
    let bobSend;

    let seq = 0;

    let aliceConfig = { url: 'wss://msg-node.ua.pt:9090/ws', runtimeURL: 'runtime:/alice-1/cluster' };
    let bobConfig = { url: 'wss://msg-node.ua.pt:9091/ws', runtimeURL: 'runtime:/bob-1/cluster' };

    let aliceProto;
    let bobProto;

    let aliceBus = {
      postMessage: (msg) => {
        console.log('postMessage(alice)', JSON.stringify(msg));
        if (seq === 0) {
          expect(msg).to.eql({
            type: 'update', from: protoAliceURL, to: 'hyperty-runtime://sp1/protostub/alice/status',
            body: {value: 'connected'}
          });

          bobProto.connect();
        }

        if (seq === 2) {
          expect(msg).to.eql({
            id: 1, type: 'ping', from: bobProto.runtimeSession, to: aliceProto.runtimeSession,
            body: { via: protoAliceURL }
          });

          aliceProto.disconnect();
          bobProto.disconnect();
          done();
        }

        seq++;
      },

      addListener: (url) => {
        console.log('addListener(alice)', url);
        expect(url).to.eql('*');
      }
    };

    let bobBus = {
      postMessage: (msg) => {
        console.log('postMessage(bob)', JSON.stringify(msg));
        if (seq === 1) {
          expect(msg).to.eql({
            type: 'update', from: protoBobURL, to: 'hyperty-runtime://sp1/protostub/bob/status',
            body: {value: 'connected'}
          });

          bobSend({id: 1, type: 'ping', from: bobProto.runtimeSession, to: aliceProto.runtimeSession});
        }

        seq++;
      },

      addListener: (url, callback) => {
        console.log('addListener(bob)', url);
        bobSend = callback;
      }
    };

    aliceProto = activate(protoAliceURL, aliceBus, aliceConfig).instance;
    bobProto = activate(protoBobURL, bobBus, bobConfig).instance;

    aliceProto.connect();
  });
});
