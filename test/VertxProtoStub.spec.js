import expect from 'expect.js';
import activate from '../src/js/client/VertxProtoStub';

describe('VertxProtoStub', function() {

  it('runtime connectivity', function(done) {
    let protoURL = 'hyperty-runtime://sp1/protostub/123';
    let send;

    let seq = 0;
    let proto;

    //TODO: requirement -> vertx MN must be online on ws://localhost:9090/ws

    let bus = {
      postMessage: (msg) => {
        if (seq === 0) {
          expect(msg).to.eql({
            type: 'update', from: protoURL, to: 'hyperty-runtime://sp1/protostub/123/status',
            body: {value: 'connected'}
          });

          //send loopback ping
          send({id: 1, type: 'ping', from: proto.runtimeSession, to: proto.runtimeSession});
        }

        if (seq === 1) {
          //if the runtime is registered, ping should arrive here
          expect(msg).to.eql({
            id: 1, type: 'ping', from: proto.runtimeSession, to: proto.runtimeSession,
            body: { via: protoURL }
          });

          proto.disconnect();
        }

        if (seq === 2) {
          expect(msg).to.eql({
            type: 'update', from: protoURL, to: 'hyperty-runtime://sp1/protostub/123/status',
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
      url: 'wss://msg-node.ua.pt:9090/ws',
      runtimeURL: 'runtime:/alice1'
    };

    proto = activate(protoURL, bus, config).instance;
    console.log(proto);
    proto.connect();
  });

  /*
  it('runtime duplicated connection', function(done) {
    let proto1;
    let proto2;

    let seq = 0;

    let bus1 = {
      postMessage: (msg) => {
        if (seq === 0) {
          expect(msg).to.eql({
            type: 'update', from: 'hyperty-runtime://sp1/protostub/1', to: 'hyperty-runtime://sp1/protostub/1/status',
            body: {value: 'connected'}
          });
        }

        if (seq === 1) {
          expect(msg).to.eql({
            type: 'update', from: 'hyperty-runtime://sp1/protostub/1', to: 'hyperty-runtime://sp1/protostub/1/status',
            body: {value: 'disconnected', desc: 'Normal closure, meaning that the purpose for which the connection was established has been fulfilled.'}
          });

          done();
        }

        seq++;
      },

      addListener: (url, callback) => {
        console.log(url);
      }
    };

    let bus2 = {
      postMessage: (msg) => {
        expect(msg).to.eql({
          type: 'update', from: 'hyperty-runtime://sp1/protostub/2', to: 'hyperty-runtime://sp1/protostub/2/status',
          body: {value: 'disconnected', desc: 'Reconnection fail. Incorrect runtime token!'}
        });

        proto1.disconnect();
      },

      addListener: (url, callback) => {
        console.log(url);
      }
    };

    let config = {
      url: 'wss://msg-node.ua.pt:9090/ws',
      runtimeURL: 'runtime:/alice-duplicated'
    };

    proto1 = new VertxProtoStub('hyperty-runtime://sp1/protostub/1', bus1, config);
    proto2 = new VertxProtoStub('hyperty-runtime://sp1/protostub/2', bus2, config);

    proto1.connect();
    proto2.connect();
  });
  */

  it('runtime re-connection', function(done) {
    let protoURL = 'hyperty-runtime://sp1/protostub/1';
    let send;
    let proto;

    let seq = 0;

    let bus = {
      postMessage: (msg) => {
        console.log(JSON.stringify(msg));
        if (seq === 0) {
          expect(msg).to.eql({
            type: 'update', from: protoURL, to: 'hyperty-runtime://sp1/protostub/1/status',
            body: {value: 'connected'}
          });

          proto._sock.close(); //simulate abnormal close
        }

        if (seq === 1) {
          expect(msg).to.eql({
            type: 'update', from: protoURL, to: 'hyperty-runtime://sp1/protostub/1/status',
            body: {value: 'disconnected', desc: 'No status code was actually present.'}
          });

          proto.connect();
        }

        if (seq === 2) {
          expect(msg).to.eql({
            type: 'update', from: protoURL, to: 'hyperty-runtime://sp1/protostub/1/status',
            body: {value: 'connected'}
          });

          send({id: 1, type: 'ping', from: proto.runtimeSession, to: proto.runtimeSession});
        }

        if (seq === 3) {
          expect(msg).to.eql({
            id: 1, type: 'ping', from: proto.runtimeSession, to: proto.runtimeSession,
            body: { via: protoURL }
          });
          proto.disconnect();
        }

        if (seq === 4) {
          expect(msg).to.eql({
            type: 'update', from: protoURL, to: 'hyperty-runtime://sp1/protostub/1/status',
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
      url: 'wss://msg-node.ua.pt:9090/ws',
      runtimeURL: 'runtime:/alice-reconnect'
    };

    proto = activate(protoURL, bus, config).instance;
    proto.connect();
  });

  it('hyperty registration', function(done) {
    let protoURL = 'hyperty-runtime://sp1/protostub/123';
    let send;
    let proto;

    let seq = 0;
    let firstURL;
    let secondURL;

    let bus = {
      postMessage: (msg) => {
        if (seq === 0) {
          expect(msg).to.eql({
            type: 'update', from: protoURL, to: 'hyperty-runtime://sp1/protostub/123/status',
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
          expect(msg).to.eql({
            id: 1, type: 'ping', from: firstURL, to: secondURL,
            body: { via: protoURL }
          });

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
      url: 'wss://msg-node.ua.pt:9090/ws',
      runtimeURL: 'runtime:/alice2'
    };

    proto = activate(protoURL, bus, config).instance;

    send({
      id: 1, type: 'create', from: 'runtime:/alice/registry/allocation', to: 'domain://msg-node.ua.pt/hyperty-address-allocation',
      body: {number: 2}
    });
  });

});
