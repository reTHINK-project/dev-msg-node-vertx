import expect from 'expect.js';
import VertxProtoStub from '../src/js/VertxProtoStub';

describe('Cluster', function() {
  it('cluster connectivity', function(done) {
    //TODO: requirement -> vertx MN must be online on (ws://localhost:9090/ws, ws://localhost:9091/ws)
    let bobSend;

    let seq = 0;

    let aliceConfig = { url: 'ws://localhost:9090/ws', runtimeURL: 'runtime:/alice-1' };
    let bobConfig = { url: 'ws://localhost:9091/ws', runtimeURL: 'runtime:/bob-1' };

    let aliceProto;
    let bobProto;

    let aliceBus = {
      postMessage: (msg) => {
        if (seq === 0) {
          expect(msg).to.eql({
            header: {type: 'update', from: 'hyperty-runtime://sp1/protostub/alice', to: 'hyperty-runtime://sp1/protostub/alice/status'},
            body: {value: 'connected'}
          });

          bobProto.connect();
        }

        if (seq === 2) {
          expect(msg).to.eql({
            header: {id: 1, type: 'ping', from: 'runtime:/bob-1', to: 'runtime:/alice-1'}
          });

          aliceProto.disconnect();
          bobProto.disconnect();
          done();
        }

        seq++;
      },

      addListener: (url, callback) => {
        expect(url).to.eql('*');
      }
    };

    let bobBus = {
      postMessage: (msg) => {
        if (seq === 1) {
          expect(msg).to.eql({
            header: {type: 'update', from: 'hyperty-runtime://sp1/protostub/bob', to: 'hyperty-runtime://sp1/protostub/bob/status'},
            body: {value: 'connected'}
          });

          bobSend({
            header: {id: 1, type: 'ping', from: 'runtime:/bob-1', to: 'runtime:/alice-1'}
          });
        }

        seq++;
      },

      addListener: (url, callback) => {
        bobSend = callback;
      }
    };

    aliceProto = new VertxProtoStub('hyperty-runtime://sp1/protostub/alice', aliceBus, aliceConfig);
    bobProto = new VertxProtoStub('hyperty-runtime://sp1/protostub/bob', bobBus, bobConfig);

    aliceProto.connect();
  });
});
