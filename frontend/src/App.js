import React, { Component } from 'react'
import axios from 'axios'
import MockAdapter from 'axios-mock-adapter'
import { BrowserRouter as Router } from 'react-router-dom'
import MainMenu from './components/Menus/MainMenu'
import './App.css'
import './Responsiveness.css'


const mock = new MockAdapter(axios)

if (process.env.NODE_ENV === 'development') {
    mock
        .onGet('/v1/activity').reply(200, {
            activity: 17
        })
        .onGet('/v1/username').reply(200, {
            username: "user"
        })
        .onGet('/v1/accounts').reply(200, {
            content: [
                {
                    title: "Fund for something",
                    balance: 250.50,
                    currency: "BGN",
                    id: 100
                },
                {
                    title: "Fund for other thing",
                    balance: 550,
                    currency: "EUR",
                    id: 101
                }
            ]
        })
        .onGet('/v1/accounts/100').reply(200,
        {
            account: {
                title: "Fund for something",
                balance: 250.50,
                currency: "BGN"
            }
        }
        )
        .onGet('/v1/accounts/101').reply(200,
        {
            account: {
                title: "Fund for other thing",
                balance: 550,
                currency: "EUR"
            }
        }
        )
        .onPost(/\/v1\/accounts\/d+\/deposit/, {
            params: {
                value: 100
            }
        }).reply(200,
        {
            message: "Operation successful"
        })
        .onPost(/\/v1\/accounts\/\d+\/deposit/, {
            params: {
                value: 100
            }
        }).reply(200,
        {
            message: "Operation successful"
        })
        .onPost(/\/v1\/accounts\/\d+\/deposit/).reply(400,
        {
            error: "Operation unsuccessful"
        })
        .onPost(/\/v1\/accounts\/\d+\/withdraw/, {
            params: {
                value: 100
            }
        }).reply(200,
        {
            message: "Operation successful"
        })
        .onPost(/\/v1\/accounts\/\d+\/withdraw/).reply(400,
        {
            error: "Operation unsuccessful"
        })
        .onPost('/v1/accounts', {
            params: {
                title: 'acc',
                currency: 'bgn'
            }
        }).reply(200, {
            message: "New account created successful"
        })
        .onPost('/v1/accounts').reply(400, {
            error: "You already have account with such a title"
        })
        .onDelete('/v1/accounts/100').reply(200, {
            message: "Operation successful"
        })
        .onDelete('/v1/accounts/101').reply(400, {
            error: "Custom error"
        })
        .onGet('/v1/transactions/1?pageSize=20').reply(200, {
            list: [
                {
                    "account": {
                        "title": "Fund for something",
                        "userId": 2830,
                        "currency": "BGN",
                        "balance": 250.5,
                        "id": 100
                    },
                    "transactions": [
                        {
                            "userId": 100,
                            "accountId": 1262,
                            "onDate": "2018-02-21T12:03:04.618",
                            "operation": "DEPOSIT",
                            "amount": 250.57
                        }
                    ]
                },
                {
                    "account": {
                        "title": "Fund for other thing",
                        "userId": 2830,
                        "currency": "EUR",
                        "balance": 550,
                        "id": 101
                    },
                    "transactions": [
                        {
                            "userId": 100,
                            "accountId": 1262,
                            "onDate": "2018-02-21T14:13:41.797",
                            "operation": "WITHDRAW",
                            "amount": 14000
                        }
                    ]
                }
            ]
        })
        .onGet(/\/v1\/transactions\/\d+/).reply(200, {
            list: [
                {
                    "account": {
                        "title": "Fund for other thing",
                        "userId": 2830,
                        "currency": "EUR",
                        "balance": 550,
                        "id": 101
                    },
                    "transactions": [
                        {
                            "userId": 100,
                            "accountId": 1262,
                            "onDate": 1518449757000,
                            "operation": "WITHDRAW",
                            "amount": 14000
                        }
                    ]
                }
            ]
        })
        .onGet('/v1/transactions/count').reply(200, { transactionsCount: 200 })
}
else {
    mock.restore()
}

export default class App extends Component {
    constructor() {
        super()
        this.state = {
            username: "",
            activity: 0
        }
    }

    updateActivity = () => {
        axios.get('/v1/activity')
            .then(response => this.setState({ activity: response.data.activity }))
            .catch(error => console.log(error))
    }

    componentDidMount() {
        setInterval(this.updateActivity, 2000)
        axios.get('/v1/username')
            .then(response => this.setState({ username: response.data.username }))
            .catch(error => console.log(error))
    }
    render() {
        return (
            <Router>
                <div className="container" >
                    <img className="logo" src={require("./images/logo.png")} />
                    <div className="greetings">
                        <h1 className="greetings__element">Welcome {this.state.username}</h1>
                        <div className="greetings__element"><hr className="greetings--splitter" /></div>
                        <h1 className="greetings__element">{this.state.activity} active users</h1>
                    </div>
                    <MainMenu />
                </div>
            </Router>
        )
    }
}
