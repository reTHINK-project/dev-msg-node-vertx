import expect from 'expect.js';
import VertxProtoStub from '../src/js/client/VertxProtoStub';

describe('VertxProtoStub', function() {
  it('runtime connectivity', function(done) {
    let send;

    let seq = 0;
    let proto;

    //TODO: requirement -> vertx MN must be online on ws://localhost:9090/ws

    let bus = {
      postMessage: (msg) => {
        if (seq === 0) {
          expect(msg).to.eql({
            type: 'update', from: 'hyperty-runtime://sp1/protostub/123', to: 'hyperty-runtime://sp1/protostub/123/status',
            body: {value: 'connected'}
          });

          //send loopback ping
          send({id: 1, type: 'ping', from: 'runtime:/alice1', to: 'runtime:/alice1'});
        }

        if (seq === 1) {
          //if the runtime is registered, ping should arrive here
          expect(msg).to.eql({id: 1, type: 'ping', from: 'runtime:/alice1', to: 'runtime:/alice1'});

          proto.disconnect();
        }

        if (seq === 2) {
          expect(msg).to.eql({
            type: 'update', from: 'hyperty-runtime://sp1/protostub/123', to: 'hyperty-runtime://sp1/protostub/123/status',
            body: {value: 'disconnected', desc: 'Normal closure, meaning that the purpose for which the connection was established has been fulfilled.'}
          });

          done();
        }

        seq++;
      },

      addListener: (url, callback) => {
        send = callback;
      }
    };

    let config = {
      url: 'ws://localhost:9090/ws',
      runtimeURL: 'runtime:/alice1'
    };

    proto = new VertxProtoStub('hyperty-runtime://sp1/protostub/123', bus, config);
    proto.connect();
  });

  /*
  it('runtime duplicated address', function(done) {
    //TODO: requirement -> vertx MN must be online on ws://localhost:9090/ws

    let bus1 = {
      postMessage: (msg) => {
        console.log(msg);
      },

      addListener: (url, callback) => {
        console.log(url);
      }
    };

    let bus2 = {
      postMessage: (msg) => {
        console.log(msg);
      },

      addListener: (url, callback) => {
        console.log(url);
      }
    };

    let config = {
      url: 'ws://localhost:9090/ws',
      runtimeURL: 'runtime:/alice-duplicated'
    };

    let proto1 = new VertxProtoStub('hyperty-runtime://sp1/protostub/1', bus1, config);
    let proto2 = new VertxProtoStub('hyperty-runtime://sp1/protostub/2', bus2, config);
    proto1.connect();
    proto2.connect();
  });
  */

  it('hyperty registration', function(done) {
    let send;

    let seq = 0;
    let firstURL;
    let secondURL;

    let bus = {
      postMessage: (msg) => {
        if (seq === 0) {
          expect(msg).to.eql({
            type: 'update', from: 'hyperty-runtime://sp1/protostub/123', to: 'hyperty-runtime://sp1/protostub/123/status',
            body: {value: 'connected'}
          });
        }

        if (seq === 1) {
          /*expect something like -> {
            id: 1, type: 'response', from: 'domain://msg-node.ua.pt/hyperty-address-allocation', to: 'runtime:/alice/registry/allocation',
            body: {code: 200, allocated: ['hyperty-instance://ua.pt/fbf7dc26-ff4f-454f-961e-22edda927561', 'hyperty-instance://ua.pt/6e8f126b-1c56-4525-9a38-5dcd340194da']}
          }*/
          expect(msg).to.eql({id: 1, type: 'response', from: 'domain://msg-node.ua.pt/hyperty-address-allocation', to: 'runtime:/alice/registry/allocation', body: msg.body});
          expect(msg.body.code).to.eql(200);
          expect(msg.body.allocated).to.have.length(2);

          firstURL = msg.body.allocated[0];
          secondURL = msg.body.allocated[1];

          send({id: 1, type: 'ping', from: firstURL, to: secondURL});
        }

        if (seq === 2) {
          expect(msg).to.eql({id: 1, type: 'ping', from: firstURL, to: secondURL});

          proto.disconnect();
          done();
        }

        seq++;
      },

      addListener: (url, callback) => {
        send = callback;
      }
    };

    let config = {
      url: 'ws://localhost:9090/ws',
      runtimeURL: 'runtime:/alice2'
    };

    let proto = new VertxProtoStub('hyperty-runtime://sp1/protostub/123', bus, config);

    send({
      id: 1, type: 'create', from: 'runtime:/alice/registry/allocation', to: 'domain://msg-node.ua.pt/hyperty-address-allocation',
      body: {number: 2}
    });
  });

});
