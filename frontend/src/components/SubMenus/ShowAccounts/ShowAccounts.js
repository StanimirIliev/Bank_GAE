import React, { Component } from 'react'
import { Link } from 'react-router-dom'
import axios from 'axios'
import Account from '../../Common/Account'
import Loading from '../../Common/Loading'
import BackButton from '../../Common/BackButton'
import './ShowAccounts.css'

class ShowAccounts extends Component {
    constructor() {
        super()
        this.state = {
            accounts: [],
            loading: false,
            error: null
        }
    }

    componentWillMount() {
        this.setState({ loading: true })
        axios.get('/v1/accounts')
            .then(resp => {
                this.setState({ loading: false, accounts: resp.data.content })
            })
            .catch(err => this.setState({ loading: false, error: err.data.msg }))
    }

    render() {
        const { loading, accounts } = this.state

        if (loading) {
            return (<Loading />)
        }
        if (accounts.length === 0) {
            return (
                <div className="container__accounts container__show_accounts" >
                    <h1 className="show_accounts__header">You have no accounts yet.</h1>
                    <Link className="linkButton button--close" to="/main">Back</Link>
                </div>
            )
        }
        const accountsRendered = []
        for (let i = 0; i < accounts.length; i++) {
            if (i === accounts.length - 1) {
                accountsRendered.push(
                    <Link key={i} className="container__accounts__item" to={`/accounts/${accounts[i].id}`}>
                        <hr className="splitter__accounts" />
                        <Account title={accounts[i].title}
                            balance={accounts[i].balance}
                            currency={accounts[i].currency} />
                        <hr className="splitter__accounts" />
                    </Link>)
            }   
            else {
                accountsRendered.push(
                    <Link key={i} className="container__accounts__item" to={`/accounts/${accounts[i].id}`}>
                        <hr className="splitter__accounts" />
                        <Account title={accounts[i].title}
                            balance={accounts[i].balance}
                            currency={accounts[i].currency} />
                    </Link>)
            }
        }
        return (
            <div className="container__accounts" >
                <h1 className="account_menu__header">My accounts:</h1>
                <div className="show_accounts__container">
                    {accountsRendered}
                </div>
                <BackButton to="/main" name="Back"/>
            </div>
        )
    }
}

export default ShowAccounts
