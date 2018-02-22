import React, { Component } from 'react'
import { Link } from 'react-router-dom'
import Loading from '../../Common/Loading'
import Msg from '../../Common/Message'
import axios from 'axios'
import Money from '../../Common/Money'
class Withdraw extends Component {
    constructor(props) {
        super(props)
        this.state = {
            id: parseInt(this.props.match.params.id),
            loading: false,
            account: {},
            error: null,
            msg: null,
            withdrawValue: null
        }
        this.handleChange = this.handleChange.bind(this)
    }

    componentWillMount() {
        this.setState({
            loading: true
        })
        axios.get(`/v1/accounts/${this.state.id}`)
            .then(resp => this.setState({
                account: resp.data.account,
                loading: false
            }))
            .catch(error => this.setState({
                loading: false,
                error: error.data.error
            }))
    }

    executeWithdraw() {
        this.setState({ loading: true })
        axios.post(`/v1/accounts/${this.state.id}/withdraw`, {
            params: {
                value: parseFloat(this.state.withdrawValue)
            }
        })
            .then(resp => this.setState({
                msg: {
                    content: resp.data.message,
                    positive: true
                },
                loading: false
            }))
            .catch(error => this.setState({
                msg: {
                    content: error.response.data.error,
                    positive: false
                },
                loading: false
            }))
    }

    handleChange(event) {
        this.setState({ withdrawValue: event.target.value })
    }

    render() {
        const { loading, account, id, msg } = this.state
        if (loading) {
            return (<Loading />)
        }
        if (msg != null) {
            return (<Msg content={msg.content} messageClass={msg.positive ? 'message--positive' : 'message--negative'} returnPath={`/accounts/${id}`} />)
        }
        return (
            <div className="container__accounts">
                <h1 className="account_menu__header">Withdraw from: {account.title}</h1>
                <div className="container__selected_account">
                    <div className="selected_account__balance">
                        <b>Balance:</b> <Money amount={account.balance} currency={account.currency} digits={2} />
                    </div>
                    <hr className="operation__splitter" />
                    <div className="container__operation" >
                        <div className="operation__name">Enter the amount of the withdraw</div>
                        <input className="operation__input" placeholder="e.g. 500"
                            onChange={this.handleChange} />
                    </div>
                </div>
                <button className="button button--execute" onClick={() => { this.executeWithdraw() }}>Execute</button>
                <Link className="linkButton button--close" to={`/accounts/${id}`}>Back</Link>
            </div>
        )
    }
}

export default Withdraw