import React from 'react'
import * as PropTypes from 'prop-types'
import SelectButtonList from '../../components/SelectButtonList'
import {
  filterButtons, filterActivityPropType as idType
} from '../../utils/activity-util'

class ActivitySelectList extends React.Component {
  static propTypes = {
    selected: idType.isRequired,
    selectItem: PropTypes.func.isRequired
  }

  render () {
    return (
      <SelectButtonList items={filterButtons}
        className='Button--secondary filterButtons'
        selected={this.props.selected}
        selectItem={this.props.selectItem}
      />
    )
  }
}

ActivitySelectList.idType = idType

export default ActivitySelectList
