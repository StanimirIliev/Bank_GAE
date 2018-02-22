import React, { Component } from 'react'
import axios from 'axios'
import Loading from '../../Common/Loading'
import Money from '../../Common/Money'
import './TransactionPage.css'
import moment from 'moment'

export default class TransactionPage extends Component {
    constructor(props) {
        super(props)
        this.state = {
            page: this.props.page,
            pageSize: this.props.pageSize,
            loading: false,
            list: []
        }
    }

    componentWillMount() {
        this.setState({ loading: true })
        axios.get(`/v1/transactions/${this.state.page}?pageSize=${this.state.pageSize}`)
            .then(response => {
                this.setState({
                    list: response.data.list,
                    loading: false
                })
            })
            .catch(error => this.setState({
                loading: false
            }))
    }

    render() {
        const { loading, list, page } = this.state
        if (loading) {
            return (<Loading />)
        }
        const transactionsRendering = []
        let numeration = 1
        for (let listIndex = 0; listIndex < list.length; listIndex++) {
            for (let transactionIndex = 0; transactionIndex < list[listIndex].transactions.length; transactionIndex++) {
                const date = new Date(list[listIndex].transactions[transactionIndex].onDate)
                const date1 = new Date(Date.UTC())
                console.log(date)
                transactionsRendering.push(
                    <tr className="table__transaction__body__rows">
                        <td className="table__transaction__columns">{(page - 1) * 20 + numeration++}</td>
                        <td className="table__transaction__columns">{moment(date).format('HH:mm MM.DD.YYYY')}</td>
                        <td className="table__transaction__columns">{list[listIndex].account.title}</td>
                        <td className="table__transaction__columns">{list[listIndex].transactions[transactionIndex].operation}</td>
                        <td className="table__transaction__columns">
                            <Money
                                amount={list[listIndex].transactions[transactionIndex].amount}
                                currency={list[listIndex].account.currency}
                                digits={2}
                            />
                        </td>
                    </tr>
                )
            }
        }

        return (
            <div className="container__transaction" >
                <table className="table__transaction">
                    <tr className="table__transaction__header__rows">
                        <th className="table__transaction__columns">&#8470;</th>
                        <th className="table__transaction__columns">Date and Time</th>
                        <th className="table__transaction__columns">Account</th>
                        <th className="table__transaction__columns">Operation</th>
                        <th className="table__transaction__columns">Amount</th>
                    </tr>
                    {transactionsRendering}
                </table>
            </div>
        )
    }
}
