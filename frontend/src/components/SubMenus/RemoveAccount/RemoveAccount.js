import React, { Component } from 'react'
import { Link } from 'react-router-dom'
import axios from 'axios'
import Loading from '../../Common/Loading'
import Message from '../../Common/Message'
import Money from '../../Common/Money'
import './RemoveAccount.css'

class AccountDetails extends Component {
    constructor(props) {
        super(props)
        this.state = {
            account: {},
            loading: false,
            error: null,
            id: parseInt(this.props.match.params.id),
            msg: null
        }
    }

    componentWillMount() {
        this.setState({ loading: true })
        axios.get(`/v1/accounts/${this.state.id}`)
            .then(resp => {
                this.setState({ loading: false, account: resp.data.account })
            })
            .catch(error => this.setState({ loading: false, error: error.data.error }))
    }

    deleteAccount() {
        this.setState({ loading: true })
        axios.delete(`/v1/accounts/${this.state.id}`)
            .then(response => this.setState({
                loading: false,
                msg: {
                    content: response.data.message,
                    positive: true
                }
            }))
            .catch(error => this.setState({
                loading: false,
                msg: {
                    content: error.response.data.error,
                    positive: false
                }
            }))
    }

    render() {
        const { loading, msg, account } = this.state
        if (loading) {
            return (<Loading />)
        }

        if (msg != null) {
            return (<Message returnPath="/delete"
                content={msg.content}
                messageClass={msg.positive ? 'message--positive' : 'message--negative'} />)
        }

        return (
            <div className="container__accounts" >
                <h1 className="account_menu__header">Are you sure you want to delete account:<br />  {account.title}</h1>
                <div className="container__selected_account">
                    <div className="selected_account__balance">
                        <b>Balance:</b> <Money amount={account.balance} currency={account.currency} digits={2} />
                    </div>
                    <div className="selected_account__buttons">
                        <button className="button remove_account__buttons" onClick={() => {
                            this.deleteAccount()
                        }}>Yes</button>
                        <Link className="linkButton remove_account__buttons" to='/delete'>No</Link>
                    </div>
                </div>
                <Link className="linkButton button--close" to="/delete">Back</Link>
            </div>
        )
    }
}

export default AccountDetails