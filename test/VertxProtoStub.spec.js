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
        seq++;
        console.log('postMessage: ', JSON.stringify(msg), seq);
        if (seq === 1) {
          expect(msg).to.eql({
            type: 'update', from: protoURL, to: 'hyperty-runtime://sp1/protostub/123/status',
            body: {value: 'created'}
          });
        }

        if (seq === 2) {
          expect(msg).to.eql({
            type: 'update', from: protoURL, to: 'hyperty-runtime://sp1/protostub/123/status',
            body: {value: 'in-progress'}
          });
        }

        if (seq === 3) {
          expect(msg).to.eql({
            type: 'update', from: protoURL, to: 'hyperty-runtime://sp1/protostub/123/status',
            body: {value: 'live'}
          });
          //send loopback ping
          send({id: 1, type: 'ping', from: proto.runtimeSession, to: proto.runtimeSession});
        }

        if (seq === 4) {
          //if the runtime is registered, ping should arrive here
          expect(msg).to.eql({
            id: 1, type: 'ping', from: proto.runtimeSession, to: proto.runtimeSession,
            body: { via: protoURL }
          });

          proto.disconnect();
        }

        if (seq === 5) {
          expect(msg).to.eql({
            type: 'update', from: protoURL, to: 'hyperty-runtime://sp1/protostub/123/status',
            body: {value: 'disconnected', desc: 'Normal closure, meaning that the purpose for which the connection was established has been fulfilled.'}
          });

          done();
        }

      },

      addListener: (url, callback) => {
        send = callback;
      }
    };

    let config = {
      url: 'wss://msg-node.localhost:9090/ws',
      runtimeURL: 'runtime:/alice1'
    };

    proto = activate(protoURL, bus, config).instance;
    console.log(proto);
    proto.connect();
  });

  it('runtime re-connection', function(done) {
    let protoURL = 'hyperty-runtime://sp1/protostub/1';
    let send;
    let proto;

    let seq = 0;

    let bus = {
      postMessage: (msg) => {
        console.log(JSON.stringify(msg), seq);
        if (seq === 0) {
          expect(msg).to.eql({
            type: 'update', from: protoURL, to: 'hyperty-runtime://sp1/protostub/1/status',
            body: {value: 'created'}
          });
        }
        if (seq === 1) {
          expect(msg).to.eql({
            type: 'update', from: protoURL, to: 'hyperty-runtime://sp1/protostub/1/status',
            body: {value: 'in-progress'}
          });
        }

        if (seq === 2) {
          expect(msg).to.eql({
            type: 'update', from: protoURL, to: 'hyperty-runtime://sp1/protostub/1/status',
            body: {value: 'live'}
          });

          proto._sock.close(); //simulate abnormal close
        }


        if (seq === 3) {
          expect(msg).to.eql({
            type: 'update', from: protoURL, to: 'hyperty-runtime://sp1/protostub/1/status',
            body: {value: 'disconnected', desc: 'No status code was actually present.'}
          });

          proto.connect();
        }

        if (seq === 4) {
          expect(msg).to.eql({
            type: 'update', from: protoURL, to: 'hyperty-runtime://sp1/protostub/1/status',
            body: {value: 'in-progress'}
          });

        }

        if (seq === 5) {
          expect(msg).to.eql({
            type: 'update', from: protoURL, to: 'hyperty-runtime://sp1/protostub/1/status',
            body: {value: 'live'}
          });

          send({id: 1, type: 'ping', from: proto.runtimeSession, to: proto.runtimeSession});
        }

        if (seq === 6) {
          expect(msg).to.eql({
            id: 1, type: 'ping', from: proto.runtimeSession, to: proto.runtimeSession,
            body: { via: protoURL }
          });
          proto.disconnect();
        }

        if (seq === 7) {
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
      url: 'wss://msg-node.localhost:9090/ws',
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
        console.log(JSON.stringify(msg), seq);
        if (seq === 0) {
          expect(msg).to.eql({
            type: 'update', from: protoURL, to: 'hyperty-runtime://sp1/protostub/123/status',
            body: {value: 'created'}
          });
        }

        if (seq === 1) {
          expect(msg).to.eql({
            type: 'update', from: protoURL, to: 'hyperty-runtime://sp1/protostub/123/status',
            body: {value: 'in-progress'}
          });
        }

        if (seq === 2) {
          expect(msg).to.eql({
            type: 'update', from: protoURL, to: 'hyperty-runtime://sp1/protostub/123/status',
            body: {value: 'live'}
          });
        }


        if (seq === 3) {
          //expect something like -> {
          //  id: 1, type: 'response', from: 'domain://msg-node.localhost/hyperty-address-allocation', to: 'runtime:/alice/registry/allocation',
          //  body: {code: 200, allocated: ['hyperty-instance://localhost/fbf7dc26-ff4f-454f-961e-22edda927561', 'hyperty-instance://localhost/6e8f126b-1c56-4525-9a38-5dcd340194da']}
          //}
          expect(msg).to.eql({id: 1, type: 'response', from: 'domain://msg-node.localhost/hyperty-address-allocation', to: 'runtime:/alice/registry/allocation', body: msg.body});
          expect(msg.body.code).to.eql(200);
          expect(msg.body.value.allocated).to.have.length(2);

          firstURL = msg.body.value.allocated[0];
          secondURL = msg.body.value.allocated[1];

          send({id: 1, type: 'ping', from: firstURL, to: secondURL});
        }

        if (seq === 4) {
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
      url: 'wss://msg-node.localhost:9090/ws',
      runtimeURL: 'runtime:/alice2'
    };

    proto = activate(protoURL, bus, config).instance;

    send({
      id: 1, type: 'create', from: 'runtime:/alice/registry/allocation', to: 'domain://msg-node.localhost/hyperty-address-allocation',
      body: { value: { number: 2 } }
    });
  });

  it('object registration', function(done) {
    let protoURL = 'hyperty-runtime://sp1/protostub/123';
    let send;
    let proto;

    let seq = 0;
    let url;
    let urlChildren;

    let bus = {
      postMessage: (msg) => {
        if (seq === 0) {
          expect(msg).to.eql({
            type: 'update', from: 'hyperty-runtime://sp1/protostub/123', to: 'hyperty-runtime://sp1/protostub/123/status',
            body: {value: 'created'}
          });
        }
        if (seq === 1) {
          expect(msg).to.eql({
            type: 'update', from: 'hyperty-runtime://sp1/protostub/123', to: 'hyperty-runtime://sp1/protostub/123/status',
            body: {value: 'in-progress'}
          });
        }
        if (seq === 2) {
          expect(msg).to.eql({
            type: 'update', from: 'hyperty-runtime://sp1/protostub/123', to: 'hyperty-runtime://sp1/protostub/123/status',
            body: {value: 'live'}
          });
        }

        if (seq === 3) {
          //expect something like -> {
          //  id: 1, type: 'response', from: 'domain://msg-node.localhost/hyperty-address-allocation', to: 'runtime:/alice/registry/allocation',
          //  body: {code: 200, allocated: ['hyperty-instance://localhost/fbf7dc26-ff4f-454f-961e-22edda927561', 'hyperty-instance://localhost/6e8f126b-1c56-4525-9a38-5dcd340194da']}
          //}
          expect(msg).to.eql({id: 1, type: 'response', from: 'domain://msg-node.localhost/object-address-allocation', to: 'runtime:/alice/registry/allocation', body: msg.body});
          expect(msg.body.code).to.eql(200);
          expect(msg.body.value.allocated).to.have.length(1);

          url = msg.body.value.allocated[0];
          urlChildren = url;

          send({id: 1, type: 'ping', from: url, to: urlChildren});
        }

        if (seq === 4) {
          expect(msg).to.eql({
            id: 1, type: 'ping', from: url, to: urlChildren,
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
      url: 'wss://msg-node.localhost:9090/ws',
      runtimeURL: 'runtime:/alice2'
    };

    proto = activate('hyperty-runtime://sp1/protostub/123', bus, config).instance;

    send({
      id: 1, type: 'create', from: 'runtime:/alice/registry/allocation', to: 'domain://msg-node.localhost/object-address-allocation',
      body: { scheme: 'fake', childrenResources: ['message'], value: { number: 1 } }
    });
  });

  it('hyperty registration with address reusage', function(done) {
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
            body: {value: 'created'}
          });
        }
        if (seq === 1) {
          expect(msg).to.eql({
            type: 'update', from: protoURL, to: 'hyperty-runtime://sp1/protostub/123/status',
            body: {value: 'in-progress'}
          });
        }
        if (seq === 2) {
          expect(msg).to.eql({
            type: 'update', from: protoURL, to: 'hyperty-runtime://sp1/protostub/123/status',
            body: {value: 'live'}
          });
        }

        if (seq === 3) {
          //expect something like -> {
          //  id: 1, type: 'response', from: 'domain://msg-node.localhost/hyperty-address-allocation', to: 'runtime:/alice/registry/allocation',
          //  body: {code: 200, allocated: ['hyperty-instance://localhost/fbf7dc26-ff4f-454f-961e-22edda927561', 'hyperty-instance://localhost/6e8f126b-1c56-4525-9a38-5dcd340194da']}
          //}
          expect(msg).to.eql({id: 1, type: 'response', from: 'domain://msg-node.localhost/address-allocation', to: 'runtime:/alice/registry/allocation', body: msg.body});
          expect(msg.body.code).to.eql(200);
          expect(msg.body.value.allocated).to.have.length(2);

          firstURL = msg.body.value.allocated[0];
          secondURL = msg.body.value.allocated[1];

          send({id: 1, type: 'ping', from: firstURL, to: secondURL});
        }

        if (seq === 4) {
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
      url: 'wss://msg-node.localhost:9090/ws',
      runtimeURL: 'runtime:/alice2'
    };

    proto = activate(protoURL, bus, config).instance;

    send({
      id: 1, type: 'create', from: 'runtime:/alice/registry/allocation', to: 'domain://msg-node.localhost/address-allocation',
      body: { value: { number: 2 } }
    });
  });

  it('object registration with address reusage', function(done) {
    let protoURL = 'hyperty-runtime://sp1/protostub/123';
    let send;
    let proto;

    let seq = 0;
    let url;
    let urlChildren;

    let bus = {
      postMessage: (msg) => {
        if (seq === 0) {
          expect(msg).to.eql({
            type: 'update', from: 'hyperty-runtime://sp1/protostub/123', to: 'hyperty-runtime://sp1/protostub/123/status',
            body: {value: 'created'}
          });
        }
        if (seq === 1) {
          expect(msg).to.eql({
            type: 'update', from: 'hyperty-runtime://sp1/protostub/123', to: 'hyperty-runtime://sp1/protostub/123/status',
            body: {value: 'in-progress'}
          });
        }
        if (seq === 2) {
          expect(msg).to.eql({
            type: 'update', from: 'hyperty-runtime://sp1/protostub/123', to: 'hyperty-runtime://sp1/protostub/123/status',
            body: {value: 'live'}
          });
        }

        if (seq === 3) {
          //expect something like -> {
          //  id: 1, type: 'response', from: 'domain://msg-node.localhost/hyperty-address-allocation', to: 'runtime:/alice/registry/allocation',
          //  body: {code: 200, allocated: ['hyperty-instance://localhost/fbf7dc26-ff4f-454f-961e-22edda927561', 'hyperty-instance://localhost/6e8f126b-1c56-4525-9a38-5dcd340194da']}
          //}
          expect(msg).to.eql({id: 1, type: 'response', from: 'domain://msg-node.localhost/address-allocation', to: 'runtime:/alice/registry/allocation', body: msg.body});
          expect(msg.body.code).to.eql(200);
          expect(msg.body.value.allocated).to.have.length(1);

          url = msg.body.value.allocated[0];
          urlChildren = url;

          send({id: 1, type: 'ping', from: url, to: urlChildren});
        }

        if (seq === 4) {
          expect(msg).to.eql({
            id: 1, type: 'ping', from: url, to: urlChildren,
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
      url: 'wss://msg-node.localhost:9090/ws',
      runtimeURL: 'runtime:/alice2'
    };

    proto = activate('hyperty-runtime://sp1/protostub/123', bus, config).instance;

    send({
      id: 1, type: 'create', from: 'runtime:/alice/registry/allocation', to: 'domain://msg-node.localhost/address-allocation',
      body: { scheme: 'fake', childrenResources: ['message'], value: { number: 1 } }
    });
  });

});
