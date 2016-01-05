import expect from 'expect.js';
import activate from '../src/js/client/VertxProtoStub';

describe('TransitionConnection', function() {
  it('connectivity between hyperties in different domains', function(done) {
    let send1;
    let send2;

    let proto1;
    let proto2;

    let hyper1;

    //TODO: requirement -> vertx MN must be online on ws://localhost:9090/ws

    let bus1 = {
      postMessage: (msg) => {
        console.log('postMessage(1): ', JSON.stringify(msg));
        if (msg.id === 1) {
          expect(msg).to.eql({id: 1, type: 'response', from: 'domain://msg-node.ua.pt/hyperty-address-allocation', to: 'runtime:/alice/registry/allocation', body: msg.body});
          expect(msg.body.code).to.eql(200);
          expect(msg.body.allocated).to.have.length(1);

          hyper1 = msg.body.allocated[0];

          //simulate message from unknown hyperty url (from the other connection)
          send2({ id: 2, type: 'ping', from: 'hyperty://unknown-url', to: hyper1 });
        }

        if (msg.id === 2) {
          expect(msg).to.eql({ id: 2, type: 'ping', from: 'hyperty://unknown-url', to: hyper1 });

          //send reply pong
          send1({ id: 1, type: 'pong', from: hyper1, to: 'hyperty://unknown-url' });
        }
      },

      addListener: (url, callback) => {
        console.log('addListener(1): ', url);
        send1 = callback;
      }
    };

    let bus2 = {
      postMessage: (msg) => {
        console.log('postMessage(2): ', JSON.stringify(msg));
        if (msg.id === 1) {
          expect(msg).to.eql({ id: 1, type: 'pong', from: hyper1, to: 'hyperty://unknown-url' });

          done();
        }
      },

      addListener: (url, callback) => {
        console.log('addListener(2): ', url);
        send2 = callback;
      }
    };

    proto1 = activate('hyperty-runtime://sp1/protostub', bus1, { url: 'wss://msg-node.ua.pt:9090/ws', runtimeURL: 'runtime:/inter-domain-1'}).instance;
    proto2 = activate('hyperty-runtime://sp2/protostub', bus2, { url: 'wss://msg-node.ua.pt:9090/ws', runtimeURL: 'runtime:/inter-domain-2'}).instance;

    send1({
      id: 1, type: 'create', from: 'runtime:/alice/registry/allocation', to: 'domain://msg-node.ua.pt/hyperty-address-allocation',
      body: { number: 1 }
    });

  });
});
