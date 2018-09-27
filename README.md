# Revolut Money Transfer Task #   
[![Build Status](https://travis-ci.com/ankulikov/revolut-money-transfer-test.svg?branch=master)](https://travis-ci.com/ankulikov/revolut-money-transfer-test)

Simple standalone application with HTTP REST API for transferring money between account with embedded database
### 1. How to start? ###
Run `mvn clean install` to compile application and run tests
 
Run `com.revolut.task.AppStarter` to launch application. It will create H2 file database (`revolut_db.mv.db`) in the project directory and 
will start server on `8080` port.  
### 2. API ###
All calls to API must be started with `http://localhost:8080/api`
                                                        
<table>
<thead>
<tr>
<th>Endpoint</th>
<th>Description</th>
<th>Parameters</th>
<th>Success Response</th>
</tr>
</thead>
<tbody>
<tr>
	<td><pre>POST /account</pre></td>
	<td>Creates new account in USD currency</td>
	<td>-</td>
	<td>
      <pre>
{
 "id": ACCOUNT_ID,
 "locked": false,
 "balance": {
   "amount": 0,
   "currency": "USD"
 }
}
	  </pre>
    </td>
</tr>
<tr>
	<td><pre>POST /account?currency=XXX</pre></td>
	<td>Creates new account in XXX currency (e.g. USD, EUR, RUB)</td>
    <td>Query:<br/><code>currency</code> - abbreviation of currency</td>
	<td>
      <pre>
{
  "id": ACCOUNT_ID,
  "locked": false,
  "balance": {
    "amount": 0,
    "currency": "XXX"
   }
}
	  </pre>
    </td>
</tr>
<tr>
	<td><pre>GET /account/{id}</pre></td>
	<td>Get account by ID</td>
    <td>Path:<br/><code>id</code> - account ID</td>
	<td>
      <pre>
{
  "id": id,
  "locked": false,
  "balance": {
    "amount": 0,
    "currency": "USD"
   }
}
	  </pre>
    </td>
</tr>
<tr>
	<td><pre>POST /account/{id}/lock</pre></td>
	<td>Lock account by ID, so account can't be used for money operations</td>
    <td>Path:<br/><code>id</code> - account ID</td>
	<td>
      204 No Content
    </td>
</tr>
<tr>
	<td><pre>POST /account/{id}/unlock</pre></td>
	<td>Unlock account by ID, so account can be used for money operations. Method do nothing for unlocked accounts</td>
    <td>Path:<br/><code>id</code> - account ID</td>
	<td>
      204 No Content
    </td>
</tr>
<tr>
	<td><pre>POST /account/{id}/deposit</pre></td>
	<td>Deposit specified amount of money on account</td>
    <td>
    	Path:<br/><code>id</code> - account ID<br/>
        Body:
        <pre>
{
  "amount": 30,
  "currency": "USD"
}
        </pre>
    </td>
	<td>
      204 No Content
    </td>
</tr>
<tr>
	<td><pre>POST /account/{id}/withdraw</pre></td>
	<td>Withdraws specified amount of money on account</td>
    <td>
    	Path:<br/><code>id</code> - account ID<br/>
        Body:
        <pre>
{
  "amount": 30,
  "currency": "RUB"
}
        </pre>
    </td>
	<td>
      204 No Content
    </td>
</tr>
<tr>
	<td><pre>POST /account/{fromId}/transfer/{toId}</pre></td>
    <td>Tranfers specified amount of money from account <code>fromId</code> to <code>toId</code></td>
    <td>
    	Path:<br/>
        <code>fromId</code> - account to withdraw from<br/>
        <code>toId</code> - account to deposit on<br/>
        Body:
        <pre>
{
  "amount": 30,
  "currency": "RUB"
}
        </pre>
    </td>
	<td>
      204 No Content
    </td>
</tr>
</tbody></table>

### 3. 3rd-party components used ###
* [Jetty](https://www.eclipse.org/jetty/) - embedded HTTP and Servlet server
* [Jersey](https://jersey.github.io) - implementation of JAX-RS API to write RESTful Web Services
* [H2 Database Engine](http://www.h2database.com/html/main.html) - Java SQL file and in-memory database
* [jOOQ](https://www.jooq.org) -  fluent API for typesafe SQL query construction and execution
* [Flyway](https://flywaydb.org) - database migration tool (used to create initial structure and fill with test data)
* [Guice](https://github.com/google/guice) - lightweight dependency framework
* [Lombok](https://projectlombok.org) - bytecode generation library to avoid of writing of the boilerplate code
* [Spock](http://spockframework.org) - testing and specification framework for Java and Groovy applications