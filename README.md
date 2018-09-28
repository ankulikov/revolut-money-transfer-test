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
	<td><code>POST /account</code></td>
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
	<td><code>POST /account?currency=XXX</code></td>
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
	<td><code>GET /account/{id}</code></td>
	<td>Gets account by ID</td>
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
	<td><code>POST /account/{id}/lock</code></td>
	<td>Locks account by ID, so account can't be used for money operations</td>
    <td>Path:<br/><code>id</code> - account ID</td>
	<td>
      204 No Content
    </td>
</tr>
<tr>
	<td><code>POST /account/{id}/unlock</code></td>
	<td>Unlocks account by ID, so account can be used for money operations. Method do nothing for unlocked accounts</td>
    <td>Path:<br/><code>id</code> - account ID</td>
	<td>
      204 No Content
    </td>
</tr>
<tr>
	<td><code>POST /account/{id}/deposit</code></td>
	<td>Deposits specified amount of money on account</td>
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
	<td><code>POST /account/{id}/withdraw</code></td>
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
	<td><code>POST /account/{fromId}/transfer/{toId}</code></td>
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
<tr>
	<td><code>DELETE /account/{id}</code></td>
	<td>Deletes account</td>
    <td>Path:<br/><code>id</code> - account ID</td>
	<td>
      204 No Content
    </td>
</tr>
</tbody></table>

#### Errors ####

<table>
<thead>
<tr>
<th>Status code</th>
<th>Response</th>
</tr>
</thead>
<tbody>
<tr>
  <td>404</td>
  <td><code>{"error":"Can't find account with ID=123"}</td>
</tr>
<tr>
  <td>400</td>
  <td><code>{"error":"Can't withdraw negative amount of money"}</td>
</tr>
<tr>
  <td>400</td>
  <td><code>{"error":"Can't deposit negative amount of money"}</td>
</tr>
<tr>
  <td>500</td>
  <td><code>{"error":"Account with ID=123 is locked"}</td>
</tr>
<tr>
  <td>500</td>
  <td><code>{"error":"Account with ID=123 doesn't have enough money to complete withdraw operation"}</td>
</tr>
<tr>
  <td>400</td>
  <td><code>{"error":"Invalid JSON request"}</td>
</tr>
<tr>
  <td>404</td>
  <td><code>{"error":"Can't find requested resource"}</td>
</tr>
<tr>
  <td>405</td>
  <td><code>{"error":"Requested resource is not allowed by specified method"}</td>
</tr>
<tr>
  <td>4**, 5**</td>
  <td><code>{"error":"ANY OTHER ERROR MESSAGE"}</td>
</tr>
</tbody>
</table>

### 3. 3rd-party components used ###
* [Jetty](https://www.eclipse.org/jetty/) - embedded HTTP and Servlet server
* [Jersey](https://jersey.github.io) - implementation of JAX-RS API to write RESTful Web Services
* [H2 Database Engine](http://www.h2database.com/html/main.html) - Java SQL file and in-memory database
* [jOOQ](https://www.jooq.org) -  fluent API for typesafe SQL query construction and execution
* [Flyway](https://flywaydb.org) - database migration tool (used to create initial structure and fill with test data)
* [Guice](https://github.com/google/guice) - lightweight dependency framework
* [Lombok](https://projectlombok.org) - bytecode generation library to avoid of writing of the boilerplate code
* [Spock](http://spockframework.org) - testing and specification framework for Java and Groovy applications