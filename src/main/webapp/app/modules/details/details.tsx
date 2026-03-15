import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Card, CardBody, Button, Badge, Spinner } from 'reactstrap';
import { useNavigate, useLocation } from 'react-router-dom';

export const Details = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const repoUrl = new URLSearchParams(location.search).get('repo') || 'DeFi-Contract';

  // We will hardcode ID 1001 for the hackathon demo, but you can pass this dynamically later
  const contractId = new URLSearchParams(location.search).get('contractId') || 1001;

  const [vulnerabilities, setVulnerabilities] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Fetch the vulnerabilities from your JHipster backend
    fetch(`/api/vulnerabilities?contractId.equals=${contractId}`)
      .then(response => response.json())
      .then(data => {
        setVulnerabilities(data);
        setLoading(false);
      })
      .catch(error => {
        console.error('Error fetching vulnerabilities:', error);
        setLoading(false);
      });
  }, []);

  // Helper function to color-code the severity badges
  const getSeverityColor = severity => {
    if (severity === 'Critical') return 'danger';
    if (severity === 'High') return 'warning';
    if (severity === 'Medium') return 'info';
    return 'secondary';
  };

  if (loading) {
    return (
      <Container className="text-center mt-5 pt-5">
        <Spinner color="primary" />
      </Container>
    );
  }

  return (
    <Container className="mt-5 pt-5 text-center">
      <h2 className="mb-2">Security Analysis Results</h2>
      <p className="text-muted mb-5">{repoUrl}</p>

      <Row className="justify-content-center">
        <Col md="10">
          <Card className="shadow-sm border-0" style={{ borderRadius: '20px' }}>
            <CardBody className="p-5">
              <h4 className="mb-5">Detected Vulnerabilities</h4>

              {vulnerabilities.map(vuln => (
                <div key={vuln.id} className="mb-4 p-4 border rounded text-start bg-light">
                  <div className="d-flex justify-content-between align-items-center mb-3">
                    <h5 style={{ fontWeight: 'bold', margin: 0 }}>{vuln.name}</h5>
                    <Badge color={getSeverityColor(vuln.severity)} style={{ fontSize: '14px', padding: '8px 12px' }}>
                      {vuln.severity}
                    </Badge>
                  </div>
                  <p className="text-muted" style={{ lineHeight: '1.6' }}>
                    {vuln.description}
                  </p>
                </div>
              ))}

              <div className="mt-5">
                <Button
                  color="success"
                  size="lg"
                  style={{ borderRadius: '30px', padding: '15px 60px', fontWeight: 'bold', fontSize: '1.2rem' }}
                  onClick={() => navigate(`/resurrect?id=${contractId}&repo=${encodeURIComponent(repoUrl)}`)}
                >
                  Generate Revived Code
                </Button>
              </div>
            </CardBody>
          </Card>
        </Col>
      </Row>
    </Container>
  );
};

export default Details;
