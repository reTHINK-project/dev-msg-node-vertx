import expect from 'expect.js';
import VertxProtoStub from '../src/js/client/VertxProtoStub';

describe('RegistryConnector', function() {
	it('registry connectivity', function(done) {
		let send;
		let proto;

		//TODO: requirement -> vertx MN must be online on ws://localhost:9090/ws

    let bus = {
			postMessage: (msg) => {
				console.log('postMessage: ', JSON.stringify(msg));

				if (msg.header.id === 2) {
					expect(msg).to.eql({
						header: { id: 2, type: 'reply', from: 'mn:/registry-connector', to: 'hyper-1' },
						body: { '123-1': { catalogAddress: '12345678', guid: '123131241241241', lastUpdate: '2015-11-30' } }
					});

					done();
				}
			},

			addListener: (url, callback) => {
				console.log('addListener: ', url);
				send = callback;
			}
		};

		let config = {
      url: 'ws://localhost:9090/ws',
      runtimeURL: 'runtime:/alice1'
    };

		proto = new VertxProtoStub('hyperty-runtime://sp1/protostub/123', bus, config);

		send({
			header: { id: 2, type: 'get-user', from: 'hyper-1', to: 'mn:/registry-connector' },
			body:{ userid: '123' }
		});
	});
});
