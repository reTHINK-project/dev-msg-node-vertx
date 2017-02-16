import expect from 'expect.js';
import activate from '../src/js/client/VertxProtoStub';

describe('TransitionConnection', function() {
  it('connectivity between hyperties in different domains', function(done) {
    let protoSP1URL = 'hyperty-runtime://sp1/protostub';
    let protoSP2URL = 'hyperty-runtime://sp2/protostub';

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
          expect(msg).to.eql({id: 1, type: 'response', from: 'domain://msg-node.localhost/hyperty-address-allocation', to: 'runtime:/alice/registry/allocation', body: msg.body});
          expect(msg.body.code).to.eql(200);
          expect(msg.body.value.allocated).to.have.length(1);

          hyper1 = msg.body.value.allocated[0];

          //simulate message from unknown hyperty url (from the other connection)
          send2({ id: 2, type: 'ping', from: 'hyperty://unknown-url', to: hyper1 });
        }

        if (msg.id === 2) {
          expect(msg).to.eql({
            id: 2, type: 'ping', from: 'hyperty://unknown-url', to: hyper1,
            body: { via: protoSP1URL }
          });

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
          expect(msg).to.eql({
            id: 1, type: 'pong', from: hyper1, to: 'hyperty://unknown-url',
            body: { via: protoSP2URL }
          });

          proto1.disconnect();
          proto2.disconnect();
          done();
        }
      },

      addListener: (url, callback) => {
        console.log('addListener(2): ', url);
        send2 = callback;
      }
    };

    proto1 = activate(protoSP1URL, bus1, { url: 'wss://msg-node.localhost:9090/ws', runtimeURL: 'runtime:/inter-domain-1'}).instance;
    proto2 = activate(protoSP2URL, bus2, { url: 'wss://msg-node.localhost:9090/ws', runtimeURL: 'runtime:/inter-domain-2'}).instance;

    send1({
      id: 1, type: 'create', from: 'runtime:/alice/registry/allocation', to: 'domain://msg-node.localhost/hyperty-address-allocation',
      body: { value: { number: 1 } }
    });

  });

  it('connectivity between hyperties in different domains with address reusage', function(done) {
    let protoSP1URL = 'hyperty-runtime://sp1/protostub';
    let protoSP2URL = 'hyperty-runtime://sp2/protostub';

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
          expect(msg).to.eql({id: 1, type: 'response', from: 'domain://msg-node.localhost/address-allocation', to: 'runtime:/alice/registry/allocation', body: msg.body});
          expect(msg.body.code).to.eql(200);
          expect(msg.body.value.allocated).to.have.length(1);

          hyper1 = msg.body.value.allocated[0];

          //simulate message from unknown hyperty url (from the other connection)
          send2({ id: 2, type: 'ping', from: 'hyperty://unknown-url', to: hyper1 });
        }

        if (msg.id === 2) {
          expect(msg).to.eql({
            id: 2, type: 'ping', from: 'hyperty://unknown-url', to: hyper1,
            body: { via: protoSP1URL }
          });

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
          expect(msg).to.eql({
            id: 1, type: 'pong', from: hyper1, to: 'hyperty://unknown-url',
            body: { via: protoSP2URL }
          });

          proto1.disconnect();
          proto2.disconnect();
          done();
        }
      },

      addListener: (url, callback) => {
        console.log('addListener(2): ', url);
        send2 = callback;
      }
    };

    proto1 = activate(protoSP1URL, bus1, { url: 'wss://msg-node.localhost:9090/ws', runtimeURL: 'runtime:/inter-domain-1'}).instance;
    proto2 = activate(protoSP2URL, bus2, { url: 'wss://msg-node.localhost:9090/ws', runtimeURL: 'runtime:/inter-domain-2'}).instance;

    send1({
      id: 1, type: 'create', from: 'runtime:/alice/registry/allocation', to: 'domain://msg-node.localhost/address-allocation',
      body: { value: { number: 1 } }
    });

  });

});
