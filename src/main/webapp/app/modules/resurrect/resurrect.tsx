import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Spinner, Card, CardBody, Badge } from 'reactstrap';
import { useLocation } from 'react-router-dom';
import axios from 'axios';

export const Resurrect = () => {
  const location = useLocation();
  const queryParams = new URLSearchParams(location.search);
  const contractId = queryParams.get('contractId') || queryParams.get('id') || 1001;

  const [contractData, setContractData] = useState<any>(null);
  const [vulnerabilities, setVulnerabilities] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [fetchError, setFetchError] = useState<string | null>(null);

  const getSeverityColor = (severity: string) => {
    if (severity === 'Critical') return 'danger';
    if (severity === 'High') return 'warning';
    if (severity === 'Medium') return 'info';
    return 'secondary';
  };

  useEffect(() => {
    const animationTimer = new Promise(resolve => setTimeout(resolve, 3000));

    const token = localStorage.getItem('jhi-authenticationToken') || sessionStorage.getItem('jhi-authenticationToken');
    const headers = token ? { Authorization: `Bearer ${token}` } : {};

    const fetchContract = axios.get(`/api/smart-contracts/${contractId}`, { headers }).then(res => res.data);
    const fetchVulns = axios
      .get(`/api/vulnerabilities?contractId.equals=${contractId}`, { headers })
      .then(res => res.data)
      .catch(() => []);

    Promise.all([animationTimer, fetchContract, fetchVulns])
      .then(([_animationComplete, contractRes, vulnsRes]) => {
        console.warn('Contract data:', contractRes);
        console.warn('Vulns data:', vulnsRes);

        if (contractRes) {
          // The backend uses _ as a newline delimiter between code lines,
          // but _ also appears in variable names like s_balance, _participate etc.
          // So we replace only the separator pattern: a _ that acts as line ending.
          // The pattern is: word-boundary _ word-boundary where _ is the line separator.
          // Safest approach: the backend stores it as sequences like }_  and ;_ and {_
          // so replace _ only when preceded by a non-underscore non-alphanumeric char,
          // or followed by whitespace/end, using a targeted regex.
          // Actually simplest: replace sequences of underscores used as blank lines (__) first,
          // then replace remaining single _ that are line separators.
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
        setVulnerabilities(vulnsRes);
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

      {/* VULNERABILITIES SECTION */}
      <Row className="justify-content-center mb-5">
        <Col md="10">
          <Card className="shadow-sm border-0" style={{ borderRadius: '15px' }}>
            <CardBody className="p-4 bg-light">
              <h4 className="mb-4 border-bottom pb-2">Identified Vulnerabilities</h4>
              {vulnerabilities.length > 0 ? (
                vulnerabilities.map(vuln => (
                  <div key={vuln.id} className="mb-3 p-3 bg-white border rounded shadow-sm">
                    <div className="d-flex justify-content-between align-items-center mb-2">
                      <h5 style={{ fontWeight: 'bold', margin: 0, color: '#dc3545' }}>{vuln.name}</h5>
                      <Badge color={getSeverityColor(vuln.severity)} style={{ fontSize: '13px', padding: '6px 10px' }}>
                        {vuln.severity}
                      </Badge>
                    </div>
                    <p className="text-muted mb-0" style={{ fontSize: '14px' }}>
                      {vuln.description}
                    </p>
                  </div>
                ))
              ) : (
                <p className="text-muted mb-0">No vulnerabilities found.</p>
              )}
            </CardBody>
          </Card>
        </Col>
      </Row>

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
