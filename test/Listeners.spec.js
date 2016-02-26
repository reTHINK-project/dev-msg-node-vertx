import expect from 'expect.js';
import activate from '../src/js/client/VertxProtoStub';

describe('Listeners', function() {
  let protoURL = 'hyperty-runtime://sp1/protostub/123';

  it('add and test listener', function(done) {
    let send;

    let seq = 0;
    let proto;

    //TODO: requirement -> vertx MN must be online on ws://localhost:9090/ws

    let bus = {
      postMessage: (msg) => {
        seq++;
        console.log('postMessage: ', JSON.stringify(msg));

        if (seq === 1) {
          expect(msg).to.eql({
            type: 'update', from: protoURL, to: 'hyperty-runtime://sp1/protostub/123/status',
            body: { value: 'connected' }
          });

          //send subscribe msg...
          send({
            id: 1, type: 'subscribe', from: 'runtime:/alice/listeners/sm', to: 'domain://msg-node.ua.pt/sm',
            body: { resource: 'resource://ua.pt/1', children: ['children1', 'children2'] }
          });
        }

        if (seq === 2) {
          expect(msg).to.eql({
            id: 1, type: 'response', from: 'domain://msg-node.ua.pt/sm', to: 'runtime:/alice/listeners/sm',
            body: { code: 200, via: protoURL }
          });

          send({ id: 2, type: 'ping', from: 'publisher', to: 'resource://ua.pt/1/changes' });
        }

        if (seq === 3) {
          expect(msg).to.eql({
            id: 2, type: 'ping', from: 'publisher', to: 'resource://ua.pt/1/changes',
            body: { via: protoURL }
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
      url: 'wss://msg-node.ua.pt:9090/ws',
      runtimeURL: 'runtime:/alice/listeners'
    };

    proto = activate(protoURL, bus, config).instance;
    proto.connect();
  });
});
