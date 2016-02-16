import expect from 'expect.js';
import activate from '../src/js/client/VertxProtoStub';

describe('RegistryConnector', function() {
	let protoURL = 'hyperty-runtime://sp1/protostub/123';
	let config = {
		url: 'wss://msg-node.ua.pt:9090/ws',
		runtimeURL: 'runtime:/alice1'
	};

	it('create user', function(done) {
		let send;

		let bus = {
			postMessage: (msg) => {
				console.log('postMessage: ', JSON.stringify(msg));

				if (msg.id === 2) {
					expect(msg).to.eql({
						id: 2, type: 'response', from: 'domain://registry.ua.pt/', to: 'hyper-1',
						body: { code: 200, via: protoURL }
					});

					done();
				}
			},

			addListener: (url, callback) => {
				console.log('addListener: ', url);
				send = callback;
			}
		};

		let proto = activate(protoURL, bus, config).activate;

		send({
			id: 2, type: 'CREATE', from: "hyper-1", to: 'domain://registry.ua.pt/',
			body: {
				value: {
					user: 'user://google.com/testuser10',
					hypertyDescriptorURL: 'hyper-1',
					hypertyURL: 'hyperty-instance://ua.pt/1'
				}
			}
		});
	});

	it('read user', function(done) {
		let send;
		let proto;

		//TODO: requirement -> vertx MN must be online on ws://localhost:9090/ws

		let bus = {
			postMessage: (msg) => {
				console.log('postMessage: ', JSON.stringify(msg));

				if (msg.id === 2) {
					expect(msg).to.eql({
						id: 2, type: 'response', from: 'domain://registry.ua.pt/', to: 'hyper-1',
						body: {
							code: 200,
							via: protoURL,
							value: {
								last: 'hyperty-instance://ua.pt/1',
								hyperties: {
									'hyperty-instance://ua.pt/1': {
										descriptor: 'hyper-1'
									}
								}
							}
						}
					});

					done();
				}
			},

			addListener: (url, callback) => {
				console.log('addListener: ', url);
				send = callback;
			}
		};

		proto = activate(protoURL, bus, config).activate;

		send({
			id: 2, type: 'READ', from: 'hyper-1', to: 'domain://registry.ua.pt/',
			body: { resource: 'user://google.com/testuser10' }
		});
	});

});
