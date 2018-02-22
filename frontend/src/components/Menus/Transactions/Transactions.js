import React, { Component } from 'react'
import { Route, Link} from 'react-router-dom'
import TransactionPage from '../../SubMenus/TransactionPage'
import Navigation from '../Transactions/Navigation'
import axios from 'axios'
import Loading from '../../Common/Loading'
import BackButton from '../../Common/BackButton'


class Transactions extends Component {
    constructor(props) {
        super(props)
        this.state = {
            pageSize: 20,
            loading: false,
            transactionsCount: 0
        }
    }

    componentWillMount() {
        this.setState({ loading: true })
        axios.get('/v1/transactions/count')
            .then(response => this.setState({
                loading: false,
                transactionsCount: response.data.transactionsCount
            }))
            .catch(error => this.setState({
                loading: false
            }))
    }

    render() {
        const { loading, pageSize, transactionsCount } = this.state
        if (loading) {
            return (<Loading />)
        }
        if (transactionsCount === 0) {
            return (
                <div className="container__accounts" >
                    <h1 className="account_menu__header">You have no transactions yet</h1>
                    <Link className="linkButton button--close" to="/">Close</Link>
                </div>
            )
        }

        const routes = []
        const pages = transactionsCount % pageSize > 0 ? parseInt(transactionsCount / pageSize) + 1 : parseInt(transactionsCount / pageSize)

        for (let i = 1; i <= pages; i++) {
            routes.push(
                <Route path={`/transactions/${i}`} render={() => <TransactionPage page={i} pageSize={pageSize} />} />
            )
        }

        return (
            <div className="container__accounts" >
                <h1 className="account_menu__header">Transaction history</h1>
                <div className="transactions__content">
                    {routes}
                </div>
                <Navigation pages={pages} />
                <BackButton to="/" name="Close"/>
            </div>
        )
    }
}

export default Transactions
