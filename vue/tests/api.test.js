import { test } from 'node:test'
import assert from 'node:assert/strict'
import { readFile } from 'node:fs/promises'

const source = (await readFile(new URL('../src/api/api.js', import.meta.url), 'utf8'))
  .replace('import.meta.env.PROD', 'true')
globalThis.window = { location: { origin: 'https://example.test' } }
const { chatAPI, chatErrorMessage, checkSession } = await import(`data:text/javascript;base64,${Buffer.from(source).toString('base64')}`)

test('login and service errors produce distinct messages', async () => {
  globalThis.fetch = async () => new Response('', { status: 401 })
  await assert.rejects(checkSession(), error => chatErrorMessage(error).includes('登录'))
  globalThis.fetch = async () => new Response('', { status: 503 })
  await assert.rejects(checkSession(), error => chatErrorMessage(error).includes('无法验证'))
  assert.match(chatErrorMessage({ name: 'TimeoutError' }), /超时/)
})

test('a failed response and an interrupted stream surface errors', async () => {
  globalThis.fetch = async () => new Response('', { status: 500 })
  await assert.rejects(chatAPI.sendAssistantMessage('hello', '1'), /500/)
  globalThis.fetch = async () => new Response(new ReadableStream({
    start(controller) { controller.error(new Error('connection lost')) }
  }))
  const reader = await chatAPI.sendAssistantMessage('hello', '1')
  await assert.rejects(reader.read(), /connection lost/)
})
