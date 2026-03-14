import React, { useState } from 'react';
import { Container, Table, Button, Badge } from 'reactstrap';
import { useNavigate, useLocation } from 'react-router-dom';

export const Results = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const queryParams = new URLSearchParams(location.search);
  const repoUrl = queryParams.get('repo') || 'Unknown Repository';

  // Mock data for your demo
  const [contracts] = useState([
    { id: 1, name: 'LendingPool.sol', date: '2023-10-12', status: 'Abandoned', vulnerabilities: 3 },
    { id: 2, name: 'YieldStaking.sol', date: '2024-01-05', status: 'Active', vulnerabilities: 0 },
    { id: 3, name: 'GovernanceToken.sol', date: '2022-08-15', status: 'Abandoned', vulnerabilities: 1 },
  ]);

  return (
    <Container className="mt-5 pt-5">
      <h2 className="mb-4">
        Review of: <small className="text-muted">{repoUrl}</small>
      </h2>

      <Table hover responsive striped>
        <thead>
          <tr>
            <th>Name</th>
            <th>Last Active</th>
            <th>Status</th>
            <th>Action</th>
          </tr>
        </thead>
        <tbody>
          {contracts.map(contract => (
            <tr key={contract.id} style={{ cursor: 'pointer' }}>
              <td>{contract.name}</td>
              <td>{contract.date}</td>
              <td>
                <Badge color={contract.status === 'Abandoned' ? 'danger' : 'success'}>{contract.status}</Badge>
              </td>
              <td>
                <Button color="primary" size="sm" onClick={() => navigate(`/details/${contract.id}`)}>
                  View Details
                </Button>
              </td>
            </tr>
          ))}
        </tbody>
      </Table>
    </Container>
  );
};

export default Results;
