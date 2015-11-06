import expect from 'expect.js';
import VertxProtoStub from '../src/js/VertxProtoStub';

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
            header: {type: 'update', from: 'hyperty-runtime://sp1/protostub/123', to: 'hyperty-runtime://sp1/protostub/123/status'},
            body: {value: 'connected'}
          });

          //send loopback ping
          send({
            header: {id: 1, type: 'ping', from: 'runtime:/alice', to: 'runtime:/alice'}
          });
        }

        if (seq === 1) {
          //if the runtime is registered, ping should arrive here
          expect(msg).to.eql({
            header: {id: 1, type: 'ping', from: 'runtime:/alice', to: 'runtime:/alice'}
          });

          proto.disconnect();
        }

        if (seq === 2) {
          expect(msg).to.eql({
            header: {type: 'update', from: 'hyperty-runtime://sp1/protostub/123', to: 'hyperty-runtime://sp1/protostub/123/status'},
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
      runtimeURL: 'runtime:/alice'
    };

    proto = new VertxProtoStub('hyperty-runtime://sp1/protostub/123', bus, config);
    proto.connect();
  });

  it('hyperty registration', function(done) {
    let send;

    let seq = 0;
    let firstURL;
    let secondURL;

    let bus = {
      postMessage: (msg) => {
        if (seq === 0) {
          expect(msg).to.eql({
            header: {type: 'update', from: 'hyperty-runtime://sp1/protostub/123', to: 'hyperty-runtime://sp1/protostub/123/status'},
            body: {value: 'connected'}
          });
        }

        if (seq === 1) {
          /*expect something like -> {
            header: {id: 1, type: 'reply', from: 'mn:/address-allocation', to: 'runtime:/alice'},
            body: {code: 'ok', allocated: ['hyperty://ua.pt/fbf7dc26-ff4f-454f-961e-22edda927561', 'hyperty://ua.pt/6e8f126b-1c56-4525-9a38-5dcd340194da']}
          }*/
          expect(msg.header).to.eql({id: 1, type: 'reply', from: 'mn:/address-allocation', to: 'runtime:/alice'});
          expect(msg.body.code).to.eql('ok');
          expect(msg.body.allocated).to.have.length(2);

          firstURL = msg.body.allocated[0];
          secondURL = msg.body.allocated[1];

          send({
            header: {id: 1, type: 'ping', from: firstURL, to: secondURL}
          });
        }

        if (seq === 2) {
          expect(msg).to.eql({
            header: {id: 1, type: 'ping', from: firstURL, to: secondURL}
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
      url: 'ws://localhost:9090/ws',
      runtimeURL: 'runtime:/alice'
    };

    let proto = new VertxProtoStub('hyperty-runtime://sp1/protostub/123', bus, config);

    send({
      header: {id: 1, type: 'create', from: 'runtime:/alice', to: 'mn:/address-allocation'},
      body: {number: 2}
    });
  });

});
