import expect from 'expect.js';
import activate from '../src/js/client/VertxProtoStub';

describe('RegistryConnector', function() {
	let protoURL = 'hyperty-runtime://sp1/protostub/123';
	let config = {
		url: 'wss://msg-node.localhost:9090/ws',
		runtimeURL: 'runtime:/alice1'
	};

	it('create user', function(done) {
		let send;

		let bus = {
			postMessage: (msg) => {
				console.log('postMessage: ', JSON.stringify(msg));

				if (msg.id === 2) {
					expect(msg).to.eql({
						id: 2, type: 'response', from: 'domain://registry.localhost/', to: 'hyper-1',
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
			id: 2, type: 'create', from: "hyper-1", to: 'domain://registry.localhost/',
			body: {
				value: {
					user: 'user://google.com/testuser10',
					url: 'hyperty-instance://localhost/1',
					expires: 3600,
					descriptor: 'hyperty-catalogue://localhost/.well-known/hyperty/hyper-1',
					dataSchemes: ['test'],
					resources:['test1', 'test2']
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
						id: 2, type: 'response', from: 'domain://registry.localhost/', to: 'hyper-1',
						body: { code: 200, via: protoURL, description: 'Not Found' }
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
			id: 2, type: 'read', from: 'hyper-1', to: 'domain://registry.localhost/',
			body: {
				auth: false,
				resource: 'user://google.com/testuser10',
				criteria : {
					dataSchemes: ['test'],
					resources:['test1', 'test2']
				}
			}
		});

	});

});
