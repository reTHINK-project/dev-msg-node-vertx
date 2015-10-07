import expect from 'expect.js';
import VertxProtoStub from '../src/js/VertxProtoStub';

describe('VertxProtoStub', function() {
  it('sending message', function(done) {
    let seq = 0;
    let proto;

    //TODO: requirement -> vertx MN must be online on ws://localhost:9090/ws

    let callback = function(msg) {
      if (seq === 0) {
        expect(msg).to.eql({
          header: {type: 'update', from: 'hyperty-runtime://sp1/protostub/123', to: 'hyperty-runtime://sp1/protostub/123/status'},
          body: {value: 'connected'}
        });
      }

      if (seq === 1) {
        expect(msg).to.eql({
          header: {id: 1, type: 'reply', to: 'hyperty-runtime://sp1/runalice'},
          body: {code: 'error', desc: 'No address alocated for \'from\' field: hyperty-runtime://sp1/runalice'}
        });

        proto.disconnect();
      }

      if (seq === 2) {
        expect(msg).to.eql({
          header: {type: 'update', from: 'hyperty-runtime://sp1/protostub/123', to: 'hyperty-runtime://sp1/protostub/123/status'},
          body: {value: 'disconnected', desc: 'No status code was actually present.'}
        });
        done();
      }

      seq++;
    };

    proto = new VertxProtoStub('hyperty-runtime://sp1/protostub/123', callback, {url: 'ws://localhost:9090/ws'});

    //register hyperty instance?
    proto.postMessage({
      header: {
        id: 1,
        type: 'create',
        from: 'hyperty-runtime://sp1/runalice',
        to: 'sp1/registry'
      },
      body: {
        hypertyURL: 'hyperty://sp1/hy123',
        hypertyInstanceURL: 'hyperty-instance://sp1/hy123',
        hypertyRuntimeURL: 'hyperty-runtime://sp1/runalice'
      }
    });

  });
});
