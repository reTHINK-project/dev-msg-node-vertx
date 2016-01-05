import expect from 'expect.js';
import activate from '../src/js/client/VertxProtoStub';

describe('RegistryConnector', function() {
	it('registry connectivity', function(done) {
		let send;
		let proto;

		//TODO: requirement -> vertx MN must be online on ws://localhost:9090/ws

		let bus = {
			postMessage: (msg) => {
				console.log('postMessage: ', JSON.stringify(msg));

				if (msg.id === 2) {
					expect(msg).to.eql({
						id: 2, type: 'response', from: 'domain://registry.ua.pt/', to: 'hyper-1',
						body: { message: 'user not found' }
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
			url: 'wss://msg-node.ua.pt:9090/ws',
			runtimeURL: 'runtime:/alice1'
		};

		proto = activate('hyperty-runtime://sp1/protostub/123', bus, config).activate;

		send({
			id: 2, type: 'READ', from: 'hyper-1', to: 'domain://registry.ua.pt/',
			body: { user: 'john@skype.com' }
		});
	});

});
