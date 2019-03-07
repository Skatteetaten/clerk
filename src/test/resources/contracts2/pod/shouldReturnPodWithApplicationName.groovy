package contracts.pod

import org.springframework.cloud.contract.spec.Contract

Contract.make {
  request {
    method 'GET'
    url $(
        stub(~/\/api\/pods\/jedi-test\?applicationName=.*/),
        test('/api/pods/jedi-test?applicationName=whoami')
    )
  }
  response {
    status 200
    headers {
      contentType(applicationJson())
    }
    body(file('responses/podForApplicationName.json'))
  }
}
