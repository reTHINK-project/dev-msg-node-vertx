import activate from '../src/js/client/VertxProtoStub';

describe('Listeners', function() {
  let config = { url: 'wss://msg-node.ua.pt:9090/ws', runtimeURL: 'runtime://domain-1/listeners' };
  let protoURL = 'hyperty-runtime://sp1/protostub';

  it('simple', function(done) {

    let number = 100000;
    let send;

    this.timeout(number + 1000);

    let sendMetrics = () => {
      let time = new Date().toJSON();
      console.log('BEGIN (' + time + ')');

      /*
      let i = 0;
      let interval = setInterval(() => {
        i++;
        if (i > number) {
          clearInterval(interval);
          return;
        }

        let time = new Date().toJSON();
        send({
          id: i, type: 'ping', from: 'hyper-1', to: 'hyper-2', body: { time: time }
        });
      });
      */

      for (let i = 0; i < number + 1; i++) {
        let time = new Date().toJSON();
        let msg = {id: i, type: 'ping', from: 'hyper-1', to: 'hyper-2', body: { time: time }};
        send(msg);
      }
    };

    let hyper1Listener = (msg) => {
      let time = new Date().toJSON();
      //console.log('Hyper-1 (' + time + '): ', msg);
      if (msg.id === number) {
        console.log('END (' + time + ')');
        done();
      }
    };

    let hyper2Listener = (msg) => {
      let time = new Date().toJSON();
      //console.log('Hyper-2 (' + time + '): ', msg);
      send({
        id: msg.id, type: 'pong', from: 'hyper-2', to: 'hyper-1', body: { time: time }
      });
    };

    let bus = {
      postMessage: (msg) => {
        if (msg.type === 'response' && msg.id === 0 && msg.body.code === 200) {
          sendMetrics();
        }

        //hyperty process...
        if (msg.to === 'hyper-1') {
          hyper1Listener(msg);
        } else if (msg.to === 'hyper-2') {
          hyper2Listener(msg);
        }

      },

      addListener: (url, callback) => { send = callback; }
    };

    let proto = activate(protoURL, bus, config).instance;
    proto.connect();

    send({
      id: 0, type: 'subscribe', from: 'runtime://metrics/test', to: 'domain://msg-node.ua.pt/sm',
      body: { subscribe: ['hyper-1', 'hyper-2'] }
    });

  });
});
