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
        console.log('postMessage: ', JSON.stringify(msg), seq);

        if (seq === 1) {
          expect(msg).to.eql({
            type: 'update', from: protoURL, to: 'hyperty-runtime://sp1/protostub/123/status',
            body: { value: 'created' }
          });

        }

        if (seq === 2) {
          expect(msg).to.eql({
              type: 'update', from: protoURL, to: 'hyperty-runtime://sp1/protostub/123/status',
              body: { value: 'in-progress' }
            }
          );
        }

        if (seq === 3) {
          expect(msg).to.eql({
              type: 'update', from: protoURL, to: 'hyperty-runtime://sp1/protostub/123/status',
              body: { value: 'live' }
            }
          );

          //send subscribe msg...
          send({
            id: 1, type: 'subscribe', from: 'runtime:/alice/listeners/sm', to: 'domain://msg-node.localhost/sm',
            body: { resource: 'resource://localhost/1', subscribe: ['resource://localhost/1', 'resource://localhost/1/changes'] }
          });
        }

        if (seq === 4) {
          expect(msg).to.eql({
            id: 1, type: 'response', from: 'domain://msg-node.localhost/sm', to: 'runtime:/alice/listeners/sm',
            body: { code: 200, via: protoURL }
          });

          send({ id: 2, type: 'ping', from: 'publisher', to: 'resource://localhost/1/changes'});
        }

        if (seq === 5) {

          expect(msg).to.eql({
            id: 2, type: 'ping', from: 'publisher', to: 'resource://localhost/1/changes',
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
      url: 'wss://msg-node.localhost:9090/ws',
      runtimeURL: 'runtime:/alice/listeners'
    };

    proto = activate(protoURL, bus, config).instance;
    proto.connect();
  });
});
