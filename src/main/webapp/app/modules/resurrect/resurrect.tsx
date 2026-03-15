import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Spinner } from 'reactstrap';
import { useLocation } from 'react-router-dom';
import axios from 'axios';

export const Resurrect = () => {
  const location = useLocation();
  const queryParams = new URLSearchParams(location.search);
  const contractId = queryParams.get('contractId') || queryParams.get('id') || 1001;

  const [contractData, setContractData] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const [fetchError, setFetchError] = useState<string | null>(null);

  useEffect(() => {
    const animationTimer = new Promise(resolve => setTimeout(resolve, 3000));

    const token = localStorage.getItem('jhi-authenticationToken') || sessionStorage.getItem('jhi-authenticationToken');
    const headers = token ? { Authorization: `Bearer ${token}` } : {};

    const fetchContract = axios.get(`/api/smart-contracts/${contractId}`, { headers }).then(res => res.data);

    Promise.all([animationTimer, fetchContract])
      .then(([_animationComplete, contractRes]) => {
        if (contractRes) {
          contractRes.originalCode = contractRes.originalCode
            ?.replace(/__/g, '\n\n')
            .replace(/_(?=[^_])/g, '\n')
            .replace(/_$/g, '\n');
          contractRes.resurrectedCode = contractRes.resurrectedCode
            ?.replace(/__/g, '\n\n')
            .replace(/_(?=[^_])/g, '\n')
            .replace(/_$/g, '\n');
        }

        setContractData(contractRes);
        setLoading(false);
      })
      .catch(error => {
        console.error('Error fetching data:', error);
        setFetchError(`Failed to load contract: ${error.message || error}`);
        setLoading(false);
      });
  }, [contractId]);

  if (loading) {
    return (
      <Container className="text-center mt-5 pt-5">
        <div style={{ height: '300px', display: 'flex', flexDirection: 'column', justifyContent: 'center', alignItems: 'center' }}>
          <Spinner color="success" style={{ width: '8rem', height: '8rem', borderWidth: '0.5rem' }} className="mb-4" />
          <h1 className="text-success mt-4">Resurrecting Protocol...</h1>
          <p className="text-muted lead">Re-writing vulnerable logic and clearing misinformation.</p>
        </div>
      </Container>
    );
  }

  if (fetchError) {
    return (
      <Container className="text-center mt-5 pt-5">
        <div style={{ color: '#dc3545', fontSize: '1.2rem' }}>
          <h3>Error Loading Contract</h3>
          <p>{fetchError}</p>
          <p className="text-muted">Contract ID attempted: {contractId}</p>
        </div>
      </Container>
    );
  }

  return (
    <Container fluid className="mt-4 px-5">
      <div className="text-center mb-5">
        <h1 className="display-4 font-weight-bold">{contractData?.name || 'Contract'} Resurrected</h1>
        <p className="lead text-muted">Vulnerabilities patched. Ready for deployment.</p>
      </div>

      {/* CODE SPLIT SCREEN */}
      <Row>
        {/* OLD CODE BOX */}
        <Col md="6">
          <div className="shadow-lg h-100" style={{ borderRadius: '15px', overflow: 'hidden' }}>
            <div className="bg-danger text-white p-3 font-weight-bold">ORIGINAL VULNERABLE CONTRACT</div>
            <div className="bg-dark text-white p-4 h-100" style={{ minHeight: '600px', overflowY: 'auto' }}>
              <pre style={{ color: '#ffb3b3', fontFamily: '"Courier New", Courier, monospace', fontSize: '14px', whiteSpace: 'pre-wrap' }}>
                {contractData?.originalCode || 'No original code found.'}
              </pre>
            </div>
          </div>
        </Col>

        {/* NEW CODE BOX */}
        <Col md="6">
          <div className="shadow-lg h-100" style={{ borderRadius: '15px', overflow: 'hidden' }}>
            <div className="bg-success text-white p-3 font-weight-bold">REVIVED SECURE CONTRACT</div>
            <div className="bg-dark text-white p-4 h-100" style={{ minHeight: '600px', overflowY: 'auto' }}>
              <pre style={{ color: '#b3ffb3', fontFamily: '"Courier New", Courier, monospace', fontSize: '14px', whiteSpace: 'pre-wrap' }}>
                {contractData?.resurrectedCode || 'No resurrected code found.'}
              </pre>
            </div>
          </div>
        </Col>
      </Row>
    </Container>
  );
};

export default Resurrect;
