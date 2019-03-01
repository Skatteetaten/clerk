package pod

import org.springframework.cloud.contract.spec.Contract

// TODO: Finner ikke podBase når jeg kjører
Contract.make {
  request {
    method 'GET'
    url $(
        stub(~/\/api\/pods\/jedi-test/),
        test('/api/pods/jedi-test')
    )
  }
  response {
    status 200
    headers {
      contentType(applicationJson())
    }
    body(file('responses/pod.json'))
  }
}

