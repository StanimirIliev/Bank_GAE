import React, { Component } from 'react'
import Loading from '../../Common/Loading'
import Message from '../../Common/Message'
import axios from 'axios'
import BackButton from '../../Common/BackButton'
import CloseButton from '../../Common/CloseButton'
import './NewAccount.css'

class NewAccount extends Component {
    constructor() {
        super()
        this.state = {
            currency: "BGN",
            title: null,
            loading: false,
            message: null
        }
        this.handleCurrencyChange = this.handleCurrencyChange.bind(this)
        this.handleTitleChange = this.handleTitleChange.bind(this)
    }

    createNewAccount() {
        this.setState({ loading: true })
        const title = this.state.title
        if (title === null || title.length > 30) {
            this.setState({
                message: {
                    content: 'Invalid title',
                    positive: false
                },
                loading: false
            })
            return
        }
        axios.post('/v1/accounts', {
            params: {
                title: this.state.title,
                currency: this.state.currency
            }
        })
            .then(resp => this.setState({
                loading: false,
                message: {
                    content: resp.data.message,
                    positive: true
                }
            }))
            .catch(error => this.setState({
                loading: false,
                message: {
                    content: error.response.data.error,
                    positive: false
                }
            }))
    }

    handleCurrencyChange(event) {
        this.setState({ currency: event.target.value })
    }

    handleTitleChange(event) {
        this.setState({ title: event.target.value })
    }

    render() {
        const { loading, message } = this.state

        if (loading) {
            return (<Loading />)
        }
        if (message != null) {
            return (<Message returnPath="/main" messageClass={message.positive ? 'message--positive' : 'message--negative'} content={message.content} />)
        }
        return (
            <div className="container__accounts">
                <h1 className="account_menu__header">Open a new account</h1>
                <div className="new_account__set_title" >
                    <div className="new_account__text">Title:</div>
                    <input className="new_account__input" onChange={this.handleTitleChange} />
                </div>
                <div className="new_account__set_currency">
                    <div className="new_account__text">Currency:</div>
                    <select className="new_account__select" name="currency" onChange={this.handleCurrencyChange}>
                        <option value="BGN">BGN</option>
                        <option value="EUR">EUR</option>
                    </select>
                </div>
                <button className="button new_account__button" onClick={() => { this.createNewAccount() }}>Create</button>
                <BackButton to="/main" />
                <CloseButton />
            </div>
        )
    }
}

export default NewAccount