import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Spinner } from 'reactstrap';
import { useLocation } from 'react-router-dom';

export const Resurrect = () => {
  const location = useLocation();
  const contractId = new URLSearchParams(location.search).get('id') || 1001;

  const [contractData, setContractData] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // 1. We create a fake delay for the "Resurrecting..." animation effect
    const animationTimer = new Promise(resolve => setTimeout(resolve, 3000));

    // 2. We actually fetch the code from the backend
    const fetchData = fetch(`/api/smart-contracts/${contractId}`).then(res => res.json());

    // Wait for BOTH the animation time and the data fetch to finish
    Promise.all([animationTimer, fetchData])
      .then(([_, data]) => {
        setContractData(data);
        setLoading(false);
      })
      .catch(error => {
        console.error('Error fetching contract:', error);
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

  return (
    <Container fluid className="mt-4 px-5">
      <div className="text-center mb-5">
        <h1 className="display-4 font-weight-bold">{contractData?.name || 'Contract'} Resurrected</h1>
        <p className="lead text-muted">Vulnerabilities patched. Ready for deployment.</p>
      </div>

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
