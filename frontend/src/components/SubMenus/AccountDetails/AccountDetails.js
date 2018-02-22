import React, { Component } from 'react'
import { Link } from 'react-router-dom'
import axios from 'axios'
import Loading from '../../Common/Loading'
import Money from '../../Common/Money'
import './AccountDetails.css'

class AccountDetails extends Component {
    constructor(props) {
        super(props)
        this.state = {
            account: {},
            loading: false,
            error: null,
            id: parseInt(this.props.match.params.id)
        }
    }

    componentWillMount() {
        this.setState({ loading: true })
        axios.get(`/v1/accounts/${this.state.id}`)
            .then(resp => {
                this.setState({ loading: false, account: resp.data.account })
            })
            .catch(error => console.log(error))
    }

    render() {
        if (this.state.loading) {
            return (<Loading />)
        }

        const currency = this.state.account.currency
        const title = this.state.account.title
        const balance = <Money amount={this.state.account.balance} currency={currency} digits={2} />
        const id = this.state.id

        return (
            <div className="container__accounts" >
                <h1 className="account_menu__header">Account:  {title}</h1>
                <div className="container__selected_account">
                    <div className="selected_account__balance">
                        <b>Balance:</b> {balance}
                    </div>
                    <div className="selected_account__buttons">
                        <Link className="linkButton selected_account__button" to={`/accounts/${id}/deposit`}>Deposit</Link>
                        <Link className="linkButton selected_account__button" to={`/accounts/${id}/withdraw`}>Withdraw</Link>
                    </div>
                </div>
                <Link className="linkButton button--close" to="/accounts">Back</Link>
            </div>
        )
    }
}

export default AccountDetails