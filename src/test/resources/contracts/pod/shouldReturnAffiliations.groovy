package pod

import org.springframework.cloud.contract.spec.Contract

Contract.make {
  request {
    method 'GET'
    url $(
        stub(~/\/api\/pods\/jedi\?applicationName=.*/),
        test('/api/pods/jedi?applicationName=paas')
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

