import expect from 'expect.js';
import activate from '../src/js/client/VertxProtoStub';

describe('RegistryConnector', function() {
	let protoURL = 'hyperty-runtime://sp1/protostub/123';
	let hyper1 = 'hyperty://hyper-1'
	let config = {
		url: 'wss://msg-node.localhost:9090/ws',
		runtimeURL: 'hyperty-runtime://alice1'
	};

	it('create user', function(done) {
		let send;

		let bus = {
			postMessage: (msg) => {
				console.log('postMessage: ', JSON.stringify(msg));

				if (msg.id === 2) {
					expect(msg).to.eql({
						id: 2, type: 'response', from: 'domain://registry.localhost', to: hyper1,
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
			id: 2, type: 'create', from: hyper1, to: 'domain://registry.localhost',
			body: {
				value: {
					user: 'user://google.com/testuser10',
					url: 'hyperty-instance://localhost/1',
					expires: 3600,
					descriptor: 'hyperty-catalogue://localhost/.well-known/hyperty/hyper-1',
					dataSchemes: ['test'],
					resources:['test1', 'test2'],
					schema : 'test',
					name: 'name',
					reporter: 'reporter',
					status:'live',
					runtime:'runtime'
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
						id: 2, type: 'response', from: 'domain://registry.localhost', to: hyper1,
						body: { code: 404, via: protoURL, description: 'Not Found' }
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
			id: 2, type: 'read', from: hyper1, to: 'domain://registry.localhost',
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
