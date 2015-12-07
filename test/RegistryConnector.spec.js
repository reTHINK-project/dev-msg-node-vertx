import expect from 'expect.js';
import VertxProtoStub from '../src/js/VertxProtoStub';

describe('Registry Connector', function() {
  it('ping test', function(done) {
    let send;

    let bus = {
      postMessage: (msg) => {
        console.log('postMessage: ', JSON.stringify(msg));

        if (msg.id === 2) {
          expect(msg).to.eql({
            id: 2, type: 'response', from: 'mn:/registry-connector', to: 'runtime:/bob1',
            body: { code: 200 }
          });

          done();
        }
      },

      addListener: (url, callback) => {
        console.log('addListener: ', url);
        send = callback;
      }
    };

    let config = {
      url: 'ws://localhost:9090/ws',
      runtimeURL: 'runtime:/bob1'
    };

    let proto = new VertxProtoStub('hyperty-runtime://sp1/protostub/123', bus, config);
    proto.connect();

    send({id: 2, type: 'ping', from: 'runtime:/bob1', to: 'mn:/registry-connector'});
  });
});
