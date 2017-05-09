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
          proto.disconnect();
        }

        if (seq === 4) {
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

          proto.disconnect();
        }

        if (seq === 6) {
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

  it('hyperty registration with address reusage', function(done) {
    let protoURL = 'hyperty-runtime://sp1/protostub/123';
    let protoURL2 = 'hyperty-runtime://sp1/protostub/1234';

    let send;
    let send2;

    let proto;
    let proto2;

    let seq = 0;
    let seq2 = 0;

    let firstURL;
    let secondURL;

    let bus = {
      postMessage: (msg) => {
        console.log('BUS 1 POSTMESSAGE(',seq,') ->', msg);
        if (msg.body.value === 'live') {
          send({
            id: 1, type: 'create', from: 'runtime:/alice/registry/allocation', to: 'domain://msg-node.localhost/address-allocation',
            body: { value: { number: 1 } }
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

          firstURL = msg.body.value.allocated[0];

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

    let bus2 = {
      postMessage: (msg) => {
        console.log('BUS 2 POSTMESSAGE(',seq2,') ->', msg);
        if (msg.body.value === 'live') {
          send2({
            id: 1, type: 'create', from: 'runtime:/alice/registry/allocation', to: 'domain://msg-node.localhost/address-allocation',
            body: { value: { number: 1 } }
          });
        }
        if (seq2 === 3) {
          //expect something like -> {
          //  id: 1, type: 'response', from: 'domain://msg-node.localhost/hyperty-address-allocation', to: 'runtime:/alice/registry/allocation',
          //  body: {code: 200, allocated: ['hyperty-instance://localhost/fbf7dc26-ff4f-454f-961e-22edda927561', 'hyperty-instance://localhost/6e8f126b-1c56-4525-9a38-5dcd340194da']}
          //}
          expect(msg).to.eql({id: 1, type: 'response', from: 'domain://msg-node.localhost/address-allocation', to: 'runtime:/alice/registry/allocation', body: msg.body});
          expect(msg.body.code).to.eql(200);
          expect(msg.body.value.allocated).to.have.length(1);

          secondURL = msg.body.value.allocated[0];

        }

        if (seq2 === 4) {
          expect(msg).to.eql({
            id: 1, type: 'ping', from: firstURL, to: secondURL,
            body: { via: protoURL2 }
          });
          done();
        }

        seq2++;
      },

      addListener: (url2, callback) => {
        send2 = callback;
      }
    };

    let config = {
      url: 'wss://msg-node.localhost:9090/ws',
      runtimeURL: 'runtime:/alice2'
    };
    let config2 = {
      url: 'wss://msg-node.localhost:9090/ws',
      runtimeURL: 'runtime:/bob2'
    };

    proto = activate(protoURL, bus, config).instance;
    proto2 = activate(protoURL2, bus2, config2).instance;
    proto2.connect();
    proto.connect();


  });

  it('object registration with address reusage', function(done) {
    let protoURL = 'hyperty-runtime://sp1/protostub/123';
    let protoURL2 = 'hyperty-runtime://sp1/protostub/1234';

    let send;
    let send2;

    let proto;
    let proto2;

    let seq = 0;
    let seq2 = 0;

    let firstURL;
    let secondURL;

    let bus = {
      postMessage: (msg) => {
        console.log('BUS 1 POSTMESSAGE(',seq,') ->', msg);

        if (msg.body.value === 'live') {
          send({
            id: 1, type: 'create', from: 'runtime:/alice2/registry/allocation', to: 'domain://msg-node.localhost/address-allocation',
            body: { scheme: 'fake', childrenResources: ['message'], value: { number: 1 } }
          });
        }

        if (seq === 3) {
          //expect something like -> {
          //  id: 1, type: 'response', from: 'domain://msg-node.localhost/hyperty-address-allocation', to: 'runtime:/alice/registry/allocation',
          //  body: {code: 200, allocated: ['hyperty-instance://localhost/fbf7dc26-ff4f-454f-961e-22edda927561', 'hyperty-instance://localhost/6e8f126b-1c56-4525-9a38-5dcd340194da']}
          //}
          expect(msg).to.eql({id: 1, type: 'response', from: 'domain://msg-node.localhost/address-allocation', to: 'runtime:/alice2/registry/allocation', body: msg.body});
          expect(msg.body.code).to.eql(200);
          expect(msg.body.value.allocated).to.have.length(1);

          firstURL = msg.body.value.allocated[0];
        }

        if (seq === 4) {
          expect(msg).to.eql({
            id: 2, type: 'ping', from: secondURL, to: firstURL,
            body: { via: protoURL }
          });
          done();
        }

        seq++;
      },

      addListener: (url, callback) => {
        send = callback;
      }
    };

    let bus2 = {
      postMessage: (msg) => {
        console.log('BUS 2 POSTMESSAGE(',seq2,') ->', msg);
        if (msg.body.value === 'live') {
          send2({
            id: 1, type: 'create', from: 'runtime:/bob2/registry/allocation', to: 'domain://msg-node.localhost/address-allocation',
            body: { scheme: 'fake', childrenResources: ['message'], value: { number: 1 } }
          });
        }
        if (seq2 === 3) {
          //expect something like -> {
          //  id: 1, type: 'response', from: 'domain://msg-node.localhost/hyperty-address-allocation', to: 'runtime:/alice/registry/allocation',
          //  body: {code: 200, allocated: ['hyperty-instance://localhost/fbf7dc26-ff4f-454f-961e-22edda927561', 'hyperty-instance://localhost/6e8f126b-1c56-4525-9a38-5dcd340194da']}
          //}
          expect(msg).to.eql({id: 1, type: 'response', from: 'domain://msg-node.localhost/address-allocation', to: 'runtime:/bob2/registry/allocation', body: msg.body});
          expect(msg.body.code).to.eql(200);
          expect(msg.body.value.allocated).to.have.length(1);

          secondURL = msg.body.value.allocated[0];
          console.log('secondURL', secondURL);
          send2({id: 2, type: 'ping', from: secondURL, to: firstURL});
        }

        seq2++;
      },

      addListener: (url2, callback) => {
        send2 = callback;
      }
    };

    let config = {
      url: 'wss://msg-node.localhost:9090/ws',
      runtimeURL: 'runtime:/alice2'
    };
    let config2 = {
      url: 'wss://msg-node.localhost:9090/ws',
      runtimeURL: 'runtime:/bob2'
    };

    proto = activate('hyperty-runtime://sp1/protostub/123', bus, config).instance;
    proto2 = activate('hyperty-runtime://sp1/protostub/1234', bus2, config2).instance;

    proto.connect();
    proto2.connect();

  });

});
