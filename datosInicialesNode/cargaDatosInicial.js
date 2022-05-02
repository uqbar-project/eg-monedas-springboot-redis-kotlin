import { createClient } from 'redis'

(async () => {

    const client = createClient()
    console.info('ejecutando scripts')

    client.on('error', (err) => {
        console.error('Error ' + err)
    })

    await client.connect()
    console.info('connected')

    await client.lPop('dolar')
    await client.rPush('dolar', '62.36', '65.43')
    await client.set('real', '13.6')
    await client.set('euro', '69.48')

    await client.quit()
    console.info('valores actualizados.')
})()

