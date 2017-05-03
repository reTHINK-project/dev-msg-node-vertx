import expect from 'expect.js';
import activate from '../src/js/client/VertxProtoStub';

describe('Listeners', function() {
  let protoURL = 'hyperty-runtime://sp1/protostub/123';
  let protoURL2 = 'hyperty-runtime://sp1/protostub/1234';

  it('add and test listener', function(done) {
    let send;

    let seq = 0;
    let seq2 = 0;
    let proto;
    let proto2;

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
            });

          //send subscribe msg...

          send({
            id: 1, type: 'subscribe', from: 'runtime:/alice/listeners/sm', to: 'domain://msg-node.localhost/sm',
            body: { resource: 'resource://localhost/1', subscribe: ['resource://localhost/1', 'resource://localhost/1/changes'] }
          });
        }
      },

      addListener: (url, callback) => {
        console.log('addListener: ', url);
        send = callback;
      }
    };

    let bus2 = {
      postMessage: (msg) => {
        seq2++;
        console.log('POSTMESSAGE2: ', JSON.stringify(msg), seq2);
        if (seq2 === 1) {
          expect(msg).to.eql({
            type: 'update', from: protoURL2, to: 'hyperty-runtime://sp1/protostub/1234/status',
            body: { value: 'created' }
          });

        }

        if (seq2 === 2) {
          expect(msg).to.eql({
              type: 'update', from: protoURL2, to: 'hyperty-runtime://sp1/protostub/1234/status',
              body: { value: 'in-progress' }
            }
          );
        }

        if (seq2 === 3) {
          expect(msg).to.eql({
              type: 'update', from: protoURL2, to: 'hyperty-runtime://sp1/protostub/1234/status',
              body: { value: 'live' }
            }
          );

          send({id: 2, type: 'ping', from: 'publisher', to: 'resource://localhost/1/changes'});

        }

        if (seq2 === 4) {
          expect(msg).to.eql({ id: 2,
              type: 'ping', from: 'publisher', to: 'resource://localhost/1/changes',
              body: { via: protoURL2 }
            }
          );
          done();

        }

      },

      addListener: (url2, callback) => {
        console.log('addListener: ', url2);
      }
    };

    let config = {
      url: 'wss://msg-node.localhost:9090/ws',
      runtimeURL: 'runtime:/alice/listeners'
    };

    proto = activate(protoURL, bus, config).instance;
    proto2 = activate(protoURL2, bus2, config).instance;
    proto.connect();
    proto2.connect();
  });
});
